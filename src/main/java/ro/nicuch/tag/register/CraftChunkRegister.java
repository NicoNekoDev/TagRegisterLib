package ro.nicuch.tag.register;

import io.github.NicoNekoDev.SimpleTuples.Quartet;
import ro.nicuch.tag.CraftTagRegister;
import ro.nicuch.tag.nbt.ChunkCompoundTag;
import ro.nicuch.tag.util.Direction;
import ro.nicuch.tag.util.PropagationType;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.RegionUUID;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class CraftChunkRegister {
    private final CraftWorldRegister register;
    private final ChunkUUID chunkId;
    private final AtomicReference<Status> status = new AtomicReference<>(Status.UNLOADED);
    private final FutureTask<ChunkCompoundTag> loadTask;

    public CraftChunkRegister(CraftWorldRegister register, ChunkUUID chunkId) {
        this.chunkId = chunkId;
        this.register = register;
        this.loadTask = new FutureTask<>(() -> {
            ChunkCompoundTag tag = register.getRegion(RegionUUID.fromChunk(chunkId)).getChunkTag(chunkId);
            this.status.set(Status.LOADED);
            return tag;
        });
    }

    public final ChunkUUID getChunkId() {
        return this.chunkId;
    }

    public final Status getStatus() {
        return this.status.get();
    }

    public final void awaitToLoad() throws ExecutionException, InterruptedException, TimeoutException {
        this.loadTask.get(30, TimeUnit.SECONDS);
    }

    public final void load() {
        this.status.set(Status.LOADING);
        CraftTagRegister.getChunkExecutor(this.chunkId.getWorkerThread(CraftTagRegister.getChunkThreads())).submit(this.loadTask);
    }

    private void propagateTo(Set<Quartet<ChunkUUID, Direction, PropagationType, Integer>> propagatedChunks, Set<ChunkUUID> visitedChunks, PropagationType type, int distance, Direction... directions) {
        for (Direction direction : directions) {
            ChunkUUID chunkUUID = this.chunkId.getRelative(direction);
            if (!visitedChunks.contains(chunkUUID)) {
                propagatedChunks.add(Quartet.of(chunkUUID, direction, type, distance));
                visitedChunks.add(chunkUUID);
            }
        }
    }

    public Set<Quartet<ChunkUUID, Direction, PropagationType, Integer>> propagate(Set<ChunkUUID> visitedChunks, Direction direction, PropagationType type, int distance) {
        Set<Quartet<ChunkUUID, Direction, PropagationType, Integer>> propagatedChunks = new LinkedHashSet<>();
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
                ChunkUUID side = this.chunkId.getRelative(direction);
                if (!visitedChunks.contains(side)) {
                    propagatedChunks.add(Quartet.of(side, direction, PropagationType.SIDE, nextDistance));
                    visitedChunks.add(side);
                }
            }
            case EDGE -> {
                this.propagateTo(propagatedChunks, visitedChunks, PropagationType.SIDE, nextDistance,
                        direction.splitToSide()
                );
                ChunkUUID edge = this.chunkId.getRelative(direction);
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
                ChunkUUID corner = this.chunkId.getRelative(direction);
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
            this.register.getRegion(RegionUUID.fromChunk(this.chunkId)).setChunkTag(this.chunkId, this.loadTask.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        this.status.set(Status.UNLOADED);
    }

    public enum Status {
        UNLOADED, LOADED, LOADING
    }
}
