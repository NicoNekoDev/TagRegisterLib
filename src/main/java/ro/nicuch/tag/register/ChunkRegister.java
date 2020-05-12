package ro.nicuch.tag.register;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.events.ChunkTagLoadEvent;
import ro.nicuch.tag.nbt.reg.ChunkCompoundTag;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.reg.RegionCompoundTag;
import ro.nicuch.tag.wrapper.BlockUUID;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.util.*;

public class ChunkRegister {
    private final RegionRegister register;
    private final Chunk chunk;
    private final ChunkCompoundTag chunkTag;
    private final Set<UUID> entities = new HashSet<>();

    private final ChunkUUID uuid;

    public final ChunkUUID getChunkUUID() {
        return this.uuid;
    }

    public ChunkRegister(RegionRegister register, Chunk chunk) {
        this.uuid = new ChunkUUID(chunk.getX(), chunk.getZ());
        this.register = register;
        this.chunk = chunk;
        RegionCompoundTag regionTag = register.getRegionTag();
        if (regionTag.containsChunkCompounds(this.uuid))
            this.chunkTag = regionTag.getChunkCompound(this.uuid);
        else {
            this.chunkTag = new ChunkCompoundTag();
        }
        for (Map.Entry<UUID, CompoundTag> entitiesEntry : this.chunkTag.entrySetEntities()) {
            UUID entityUUID = entitiesEntry.getKey();
            register.getWorldRegister().loadEntity(entityUUID, entitiesEntry.getValue());
            this.entities.add(entityUUID);
        }
        Bukkit.getScheduler().runTask(TagRegister.getPlugin(), () ->
                Bukkit.getPluginManager().callEvent(new ChunkTagLoadEvent(this, this.chunkTag)));
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    public int getX() {
        return this.chunk.getX();
    }

    public int getZ() {
        return this.chunk.getZ();
    }

    public void unload(boolean checkEntities, Set<UUID> entitiesArray) {
        this.savePopulation(checkEntities, entitiesArray);
        this.entities.clear();
    }

    public void savePopulation(boolean checkEntities, Set<UUID> entities) {
        WorldRegister worldRegister = this.register.getWorldRegister();
        if (checkEntities) {
            for (UUID uuid : entities)
                worldRegister.getStoredEntity(uuid).ifPresent(compoundTag -> {
                    if (!compoundTag.isEmpty())
                        this.chunkTag.putEntity(uuid, compoundTag);
                });
        } else {
            for (UUID uuid : this.entities) {
                if (worldRegister.isEntityStored(uuid)) {
                    this.chunkTag.putEntity(uuid, worldRegister.unloadEntity(uuid));
                }
            }
        }
        RegionCompoundTag regionTag = this.register.getRegionTag();
        if (!this.chunkTag.isEmpty(true))
            regionTag.putChunkCompound(this.uuid, this.chunkTag);
        else if (regionTag.containsChunkCompounds(uuid))
            regionTag.removeChunkCompound(uuid);
    }

    public boolean isBlockStored(Block block) {
        BlockUUID blockUUID = new BlockUUID(block.getX(), block.getY(), block.getZ());
        return this.chunkTag.containsBlock(blockUUID);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block.getX(), block.getY(), block.getZ());
        return Optional.ofNullable(this.chunkTag.getBlock(blockUUID));
    }

    public CompoundTag createStoredBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block.getX(), block.getY(), block.getZ());
        CompoundTag tag = new CompoundTag();
        this.chunkTag.putBlock(blockUUID, tag);
        return tag;
    }

    public Set<UUID> getStoredEntitiesOnThisChunk() {
        return this.entities;
    }

    public boolean isEntityStored(Entity entity) {
        return this.isEntityStored(entity.getUniqueId());
    }

    public Optional<CompoundTag> getStoredEntity(Entity entity) {
        return this.getStoredEntity(entity.getUniqueId());
    }

    public CompoundTag createStoredEntity(Entity entity) {
        return this.createStoredEntity(entity.getUniqueId());
    }

    public boolean isEntityStored(UUID uuid) {
        return this.register.isEntityStored(uuid);
    }

    public Optional<CompoundTag> getStoredEntity(UUID uuid) {
        return this.register.getStoredEntity(uuid);
    }

    public CompoundTag createStoredEntity(UUID uuid) {
        return this.register.createStoredEntity(uuid);
    }

    public RegionRegister getRegionRegister() {
        return this.register;
    }

    public ChunkCompoundTag getChunkTag() {
        return this.chunkTag;
    }

    @Override
    public String toString() {
        return "ChunkRegister{" +
                "x: " + this.uuid.getX() +
                ", y: " + this.uuid.getZ() +
                ", w: " + this.register.getWorldRegister().getWorldInstance().getName() +
                "}";
    }
}
