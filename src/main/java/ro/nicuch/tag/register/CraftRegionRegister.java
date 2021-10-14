package ro.nicuch.tag.register;

import ro.nicuch.tag.CraftRegisterExecutors;
import ro.nicuch.tag.nbt.ChunkCompoundTag;
import ro.nicuch.tag.nbt.region.RegionFile;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.RegionUUID;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

public class CraftRegionRegister {
    private final RegionUUID regionId;
    private final AtomicReference<Status> status = new AtomicReference<>(Status.UNLOADED);
    private final FutureTask<RegionFile> loadTask;

    public CraftRegionRegister(CraftWorldRegister register, RegionUUID regionId) {
        this.regionId = regionId;
        this.loadTask = new FutureTask<>(() -> {
            RegionFile region = new RegionFile(register.getWorldFolder().toPath(), this.regionId);
            this.status.set(Status.LOADED);
            return region;
        });
    }

    public final RegionUUID getRegionId() {
        return this.regionId;
    }

    public final Status getStatus() {
        return this.status.get();
    }

    public final void load() {
        this.status.set(Status.LOADING);
        CraftRegisterExecutors.getRegionExecutor().submit(this.loadTask);
    }

    public final ChunkCompoundTag getChunkTag(final ChunkUUID chunkId) {
        if (this.status.get() == Status.UNLOADED)
            this.load();
        try {
            return this.loadTask.get().getChunkCompoundTag(chunkId.getX(), chunkId.getY(), chunkId.getZ());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final void setChunkTag(final ChunkUUID chunkId, ChunkCompoundTag tag) {
        if (this.status.get() == Status.UNLOADED)
            this.load();
        try {
            this.loadTask.get().putChunkCompoundTag(chunkId.getX(), chunkId.getY(), chunkId.getZ(), tag);
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
