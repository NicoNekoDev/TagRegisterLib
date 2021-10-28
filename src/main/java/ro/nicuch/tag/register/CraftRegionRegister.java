package ro.nicuch.tag.register;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import ro.nicuch.tag.CraftTagRegister;
import ro.nicuch.tag.util.RegionFile;
import ro.nicuch.tag.wrapper.ChunkPos;
import ro.nicuch.tag.wrapper.RegionPos;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public class CraftRegionRegister {
    private final RegionPos regionId;
    private final AtomicReference<Status> status = new AtomicReference<>(Status.UNLOADED);
    private final FutureTask<RegionFile> loadTask;

    public CraftRegionRegister(CraftWorldRegister register, RegionPos regionId) {
        this.regionId = regionId;
        this.loadTask = new FutureTask<>(() -> {
            RegionFile region = new RegionFile(register.getWorldFolder().toPath(), this.regionId);
            this.status.set(Status.LOADED);
            return region;
        });
    }

    public final RegionPos getRegionId() {
        return this.regionId;
    }

    public final Status getStatus() {
        return this.status.get();
    }

    public final void load() {
        this.status.set(Status.LOADING);
        CraftTagRegister.getRegionExecutor().submit(this.loadTask);
    }

    public final Chunk getChunk(final ChunkPos chunkPos) {
        if (this.status.get() == Status.UNLOADED)
            this.load();
        try {
            return this.loadTask.get().getChunkCompoundTag(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final void setChunk(final ChunkPos chunkPos, Chunk chunk) {
        if (this.status.get() == Status.UNLOADED)
            this.load();
        try {
            this.loadTask.get().putChunkCompoundTag(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunk);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public final void unloadAndSave() {
        if (this.status.get() == Status.UNLOADED)
            return; // hmm...
        try {
            this.loadTask.get().close();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        this.status.set(Status.UNLOADED);
    }

    public enum Status {
        UNLOADED, LOADING, LOADED
    }
}
