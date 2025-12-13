package ro.nico.tag.register;

import io.github.NicoNekoDev.SimpleTuples.Quartet;
import ro.nico.tag.CraftTagRegister;
import ro.nico.tag.nbt.ChunkCompoundTag;
import ro.nico.tag.util.Direction;
import ro.nico.tag.util.PropagationType;
import ro.nico.tag.wrapper.ChunkPos;
import ro.nico.tag.wrapper.RegionPos;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class CraftChunkRegister {
    private final CraftWorldRegister register;
    private final ChunkPos chunkId;
    private final AtomicReference<Status> status = new AtomicReference<>(Status.UNLOADED);
    private final FutureTask<ChunkCompoundTag> loadTask;

    public CraftChunkRegister(CraftWorldRegister register, ChunkPos chunkId) {
        this.chunkId = chunkId;
        this.register = register;
        this.loadTask = new FutureTask<>(() -> {
            ChunkCompoundTag tag = register.getRegion(RegionPos.fromChunk(chunkId)).getChunk(chunkId);
            this.status.set(Status.LOADED);
            return tag;
        });
    }

    public final ChunkPos getChunkId() {
        return this.chunkId;
    }

    public final Status getStatus() {
        return this.status.get();
    }

    public final ChunkCompoundTag loadAndWait() throws ExecutionException, InterruptedException, TimeoutException {
        return this.loadTask.get(30, TimeUnit.SECONDS);
    }

    public final void load() {
        this.status.set(Status.LOADING);
        CraftTagRegister.getChunkExecutor(this.chunkId.getWorkerThread(CraftTagRegister.getChunkThreads())).submit(this.loadTask);
    }

    private void propagateTo(Set<Quartet<ChunkPos, Direction, PropagationType, Integer>> propagatedChunks, Set<ChunkPos> visitedChunks, PropagationType type, int distance, Direction... directions) {
        for (Direction direction : directions) {
            ChunkPos chunkPos = this.chunkId.getRelative(direction);
            if (!visitedChunks.contains(chunkPos)) {
                propagatedChunks.add(Quartet.of(chunkPos, direction, type, distance));
                visitedChunks.add(chunkPos);
            }
        }
    }

    public Set<Quartet<ChunkPos, Direction, PropagationType, Integer>> propagate(Set<ChunkPos> visitedChunks, Direction direction, PropagationType type, int distance) {
        Set<Quartet<ChunkPos, Direction, PropagationType, Integer>> propagatedChunks = new LinkedHashSet<>();
        // NOTE: propagation order is important! sides and then corners!
        int nextDistance = distance - 1;
        switch (type) {
            case CENTER -> {
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.SIDE, nextDistance,
                        Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST
                );
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.EDGE, nextDistance,
                        Direction.NORTH_EAST, Direction.NORTH_WEST, Direction.SOUTH_EAST, Direction.SOUTH_WEST,
                        Direction.NORTH_UP, Direction.SOUTH_UP, Direction.WEST_UP, Direction.EAST_UP,
                        Direction.NORTH_DOWN, Direction.SOUTH_DOWN, Direction.WEST_DOWN, Direction.EAST_DOWN
                );
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.CORNER, nextDistance,
                        Direction.NORTH_EAST_UP, Direction.NORTH_WEST_UP, Direction.SOUTH_EAST_UP, Direction.SOUTH_WEST_UP,
                        Direction.NORTH_EAST_DOWN, Direction.NORTH_WEST_DOWN, Direction.SOUTH_EAST_DOWN, Direction.SOUTH_WEST_DOWN
                );
            }
            case SIDE -> {
                ChunkPos side = this.chunkId.getRelative(direction);
                if (!visitedChunks.contains(side)) {
                    propagatedChunks.add(Quartet.of(side, direction, PropagationType.SIDE, nextDistance));
                    visitedChunks.add(side);
                }
            }
            case EDGE -> {
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.SIDE, nextDistance,
                        direction.splitToSide()
                );
                ChunkPos edge = this.chunkId.getRelative(direction);
                if (!visitedChunks.contains(edge)) {
                    propagatedChunks.add(Quartet.of(edge, direction, PropagationType.EDGE, nextDistance));
                    visitedChunks.add(edge);
                }
            }
            case CORNER -> {
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.SIDE, nextDistance,
                        direction.splitToSide()
                );
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.EDGE, nextDistance,
                        direction.splitToEdge()
                );
                ChunkPos corner = this.chunkId.getRelative(direction);
                if (!visitedChunks.contains(corner)) {
                    propagatedChunks.add(Quartet.of(corner, direction, PropagationType.CORNER, nextDistance));
                    visitedChunks.add(corner);
                }
            }
        }
        return propagatedChunks;
    }

    public final void unloadAndSave() {
        if (this.status.get() == Status.UNLOADED)
            return; // hmm...
        try {
            this.register.getRegion(RegionPos.fromChunk(this.chunkId)).setChunk(this.chunkId, this.loadTask.get());
        } catch (InterruptedException | ExecutionException ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to unload/save chunk", ex);
        }
        this.status.set(Status.UNLOADED);
    }

    public enum Status {
        UNLOADED, LOADED, LOADING
    }
}
