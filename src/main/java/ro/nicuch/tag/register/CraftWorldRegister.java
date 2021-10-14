package ro.nicuch.tag.register;

import com.google.common.cache.*;
import io.github.NicoNekoDev.SimpleTuples.Quartet;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ro.nicuch.tag.grid.Direction;
import ro.nicuch.tag.grid.PropagationType;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.RegionUUID;
import ro.nicuch.tag.wrapper.WorldUUID;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CraftWorldRegister {
    private final WorldUUID worldId;
    private final File worldDataFolder;
    private final Map<Player, Quartet<ChunkUUID, Direction, PropagationType, Integer>> playerChunks = new HashMap<>();
    private final LoadingCache<RegionUUID, CraftRegionRegister> regions;
    private final LoadingCache<ChunkUUID, CraftChunkRegister> chunks;

    private final static int maximumDistance = 10;

    public CraftWorldRegister(World world) {
        this.worldId = WorldUUID.fromWorld(world);
        this.worldDataFolder = new File(world.getWorldFolder().getPath() + File.separator + "tags");
        this.worldDataFolder.mkdirs();
        this.regions = CacheBuilder.newBuilder()
                .expireAfterAccess(20, TimeUnit.SECONDS)
                .removalListener((RemovalListener<RegionUUID, CraftRegionRegister>) removalNotification -> {
                    if (removalNotification.getCause() == RemovalCause.EXPIRED)
                        removalNotification.getValue().unloadAndSave();
                })
                .build(new CacheLoader<>() {
                    @Override
                    public CraftRegionRegister load(@NotNull RegionUUID regionUUID) {
                        return new CraftRegionRegister(CraftWorldRegister.this, regionUUID);
                    }
                });
        this.chunks = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .removalListener((RemovalListener<ChunkUUID, CraftChunkRegister>) removalNotification -> {
                    if (removalNotification.getCause() == RemovalCause.EXPIRED)
                        removalNotification.getValue().unloadAndSave();
                })
                .build(new CacheLoader<>() {
                    @Override
                    public CraftChunkRegister load(@NotNull ChunkUUID chunkUUID) {
                        return new CraftChunkRegister(CraftWorldRegister.this, chunkUUID);
                    }
                });
    }

    public final WorldUUID getWorldId() {
        return this.worldId;
    }

    public final File getWorldFolder() {
        return this.worldDataFolder;
    }

    public void updatePlayerChunk(Player player, ChunkUUID id) {
        this.playerChunks.put(player, Quartet.of(id, Direction.CENTER, PropagationType.CENTER, maximumDistance));
    }

    public CraftRegionRegister getRegion(RegionUUID regionUUID) {
        return this.regions.getUnchecked(regionUUID);
    }

    // prop = propagation
    public void propTick() {
        Set<ChunkUUID> visitedChunks = new HashSet<>(((2 * maximumDistance) + 1) ^ 3);
        for (Quartet<ChunkUUID, Direction, PropagationType, Integer> preloaded : this.playerChunks.values()) {
            visitedChunks.add(preloaded.getFirstValue()); // add main chunks as visited
        }

        Queue<Quartet<ChunkUUID, Direction, PropagationType, Integer>> queue = new LinkedList<>(this.playerChunks.values()); // queue, allow addition and removal while iterating
        Quartet<ChunkUUID, Direction, PropagationType, Integer> next = queue.poll();
        while (next != null) { // never null if we keep adding chunks
            ChunkUUID chunkUUID = next.getFirstValue();
            Direction direction = next.getSecondValue();
            PropagationType propagationType = next.getThirdValue();
            int distance = next.getForthValue();

            if (distance > 0) { // maximum distance
                CraftChunkRegister craftChunkRegister = this.chunks.getUnchecked(chunkUUID);

                switch (craftChunkRegister.getStatus()) {
                    case LOADED -> queue.addAll(craftChunkRegister.propagate(visitedChunks, direction, propagationType, distance));
                    case UNLOADED -> craftChunkRegister.load();
                }
            }

            next = queue.poll();
        }
    }
}
