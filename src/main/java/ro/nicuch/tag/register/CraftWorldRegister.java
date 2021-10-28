package ro.nicuch.tag.register;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import org.jetbrains.annotations.NotNull;
import ro.nicuch.tag.CraftTagRegister;
import ro.nicuch.tag.util.*;
import ro.nicuch.tag.wrapper.ChunkPos;
import ro.nicuch.tag.wrapper.RegionPos;
import ro.nicuch.tag.wrapper.WorldId;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CraftWorldRegister {
    private final WorldId worldId;
    private final File worldDataFolder;
    private final SelfExpiringMap<ChunkPos, ChunkTicket> chunkTickets;
    private final LoadingCache<RegionPos, CraftRegionRegister> regions;
    private final LoadingCache<ChunkPos, CraftChunkRegister> chunks;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("World-Scheduler-%d").build());

    public CraftWorldRegister(WorldId worldId) {
        this.worldId = worldId;
        this.worldDataFolder = new File("." + File.separator + this.worldId.getName() + File.separator + "tags");
        this.worldDataFolder.mkdirs();
        this.regions = CacheBuilder.newBuilder()
                .expireAfterAccess(45, TimeUnit.SECONDS) // highest ticket type time + chunk timeout + 5 seconds
                .removalListener((RemovalListener<RegionPos, CraftRegionRegister>) removalNotification -> {
                    if (removalNotification.getCause() == RemovalCause.EXPIRED)
                        if (removalNotification.getValue() != null) {
                            removalNotification.getValue().unloadAndSave();
                        }
                })
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull CraftRegionRegister load(@NotNull RegionPos regionPos) {
                        return new CraftRegionRegister(CraftWorldRegister.this, regionPos);
                    }
                });
        this.chunks = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .removalListener((RemovalListener<ChunkPos, CraftChunkRegister>) removalNotification -> {
                    if (removalNotification.getCause() == RemovalCause.EXPIRED)
                        if (removalNotification.getValue() != null) {
                            removalNotification.getValue().unloadAndSave();
                        }
                })
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull CraftChunkRegister load(@NotNull ChunkPos chunkPos) {
                        return new CraftChunkRegister(CraftWorldRegister.this, chunkPos);
                    }
                });
        this.chunkTickets = new SelfExpiringMap<>((key, value, type) -> {
            // System.out.println("REMOVED " + key.toString() + " WITH " + type.name().toLowerCase());
        }, this.scheduler);
        this.scheduler.scheduleAtFixedRate(this::propagationTick, 1, 1, TimeUnit.MILLISECONDS);
    }

    public final WorldId getWorldId() {
        return this.worldId;
    }

    public final File getWorldFolder() {
        return this.worldDataFolder;
    }

    public void setChunkTicket(ChunkPos chunkId, TicketType ticketType) {
        this.setChunkTicket(chunkId, ticketType, ticketType.getDefaultDistance());
    }

    public void setChunkTicket(ChunkPos chunkId, TicketType ticketType, int distance) {
        this.setChunkTicket(chunkId, ticketType, distance, ticketType.getDefaultTime(), TimeUnit.SECONDS);
    }

    public void setChunkTicket(ChunkPos chunkId, TicketType ticketType, int distance, long timeToLive, TimeUnit timeUnit) {
        // if priority of ticket is higher than old, replace
        ChunkTicket oldTicket = this.chunkTickets.get(chunkId);
        if (oldTicket != null)
            if (ticketType.compareTo(oldTicket.getTicketType()) > 0) {
                this.chunkTickets.renewKey(chunkId, oldTicket.getTime(), oldTicket.getTimeUnit());
                return;
            }
        this.chunkTickets.put(chunkId, new ChunkTicket(chunkId, ticketType, distance, timeToLive, timeUnit), timeToLive, timeUnit);
    }

    public void removeChunkTicket(ChunkPos chunkId) {
        this.chunkTickets.remove(chunkId);
    }

    public boolean containsChunkTicket(ChunkPos chunkId) {
        return this.chunkTickets.containsKey(chunkId);
    }

    public CraftChunkRegister getChunkRegister(ChunkPos chunkId) {
        CraftChunkRegister chunk = this.chunks.getUnchecked(chunkId);
        if (chunk.getStatus() == CraftChunkRegister.Status.UNLOADED)
            this.setChunkTicket(chunkId, TicketType.OTHER); // doesn't matter if it was loading or not
        try {
            chunk.loadAndWait();
        } catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException("Chunk failed to load!");
        } catch (TimeoutException e) {
            throw new IllegalStateException("Chunk failed to load due to timeout!");
        }
        return chunk;
    }

    public CraftRegionRegister getRegion(RegionPos regionPos) {
        return this.regions.getUnchecked(regionPos);
    }

    // prop = propagation
    public void propagationTick() {
        this.chunks.cleanUp(); // do cleanup
        Set<ChunkPos> visitedChunks = new HashSet<>((((2 * CraftTagRegister.MAXIMUM_DISTANCE) + 1) ^ 3) * this.chunkTickets.size());
        // add main chunks as visited
        visitedChunks.addAll(this.chunkTickets.keySet());
        Queue<Quartet<ChunkPos, Direction, PropagationType, Integer>> queue =
                this.chunkTickets.values().stream()
                        .map((ticket) -> Quartet.of(ticket.getChunkId(), Direction.CENTER, PropagationType.CENTER, ticket.getDistance()))
                        .collect(Collectors.toCollection(LinkedList::new)); // queue, allow addition and removal while iterating
        Quartet<ChunkPos, Direction, PropagationType, Integer> next = queue.poll();
        while (next != null) { // never null if we keep adding chunks
            ChunkPos chunkId = next.getFirstValue();
            Direction direction = next.getSecondValue();
            PropagationType propagationType = next.getThirdValue();
            int distance = next.getForthValue();

            if (distance > 0) { // maximum distance
                CraftChunkRegister craftChunkRegister = this.chunks.getUnchecked(chunkId);

                switch (craftChunkRegister.getStatus()) {
                    case LOADED -> queue.addAll(craftChunkRegister.propagate(visitedChunks, direction, propagationType, distance));
                    case UNLOADED -> craftChunkRegister.load();
                }
            }

            next = queue.poll();
        }
    }

    public final void unloadAndSave() {
        try {
            this.scheduler.shutdown();
            this.scheduler.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Failed to unload and save the world due to timeout!");
        }
    }
}
