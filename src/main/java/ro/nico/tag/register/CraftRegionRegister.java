package ro.nico.tag.register;

import ro.nico.tag.CraftTagRegister;
import ro.nico.tag.nbt.tags.collection.CompoundTag;
import ro.nico.tag.util.RegionFile;
import ro.nico.tag.wrapper.ChunkPos;
import ro.nico.tag.wrapper.RegionPos;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

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

    public final CompoundTag getChunk(final ChunkPos chunkPos) {
        if (this.status.get() == Status.UNLOADED)
            this.load();
        try {
            return this.loadTask.get().getChunkCompoundTag(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
        } catch (InterruptedException | ExecutionException ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to get chunk in region", ex);
        }
        return null;
    }

    public final void setChunk(final ChunkPos chunkPos, CompoundTag chunk) {
        if (this.status.get() == Status.UNLOADED)
            this.load();
        try {
            this.loadTask.get().putChunkCompoundTag(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunk);
        } catch (InterruptedException | ExecutionException ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to put chunk in region", ex);
        }
    }

    public final void unloadAndSave() {
        if (this.status.get() == Status.UNLOADED)
            return; // hmm...
        try {
            this.loadTask.get().close();
        } catch (InterruptedException | ExecutionException | IOException ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to unload/save region", ex);
        }
        this.status.set(Status.UNLOADED);
    }

    public enum Status {
        UNLOADED, LOADING, LOADED
    }
}
