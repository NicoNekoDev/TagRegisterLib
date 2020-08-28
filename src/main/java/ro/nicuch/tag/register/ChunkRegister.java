package ro.nicuch.tag.register;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.tag.nbt.ChunkCompoundTag;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.region.RegionFile;
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
        this(register, chunk, new ChunkUUID(chunk.getX(), chunk.getZ()));
    }

    public ChunkRegister(RegionRegister register, Chunk chunk, ChunkUUID uuid) {
        this.uuid = uuid;
        this.register = register;
        this.chunk = chunk;
        RegionFile regionTag = register.getRegionFile();
        if (regionTag.containsChunkCompounds(this.uuid))
            this.chunkTag = regionTag.getChunkCompound(this.uuid);
        else {
            this.chunkTag = new ChunkCompoundTag();
        }
        for (Map.Entry<UUID, CompoundTag> entitiesEntry : this.chunkTag.entrySetEntities()) {
            UUID entityUUID = entitiesEntry.getKey();
            register.getWorldRegister().loadEntityInternal(entityUUID, entitiesEntry.getValue());
            this.entities.add(entityUUID);
        }
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
            this.chunkTag.clearEntities();
            for (UUID uuid : entities) {
                worldRegister.getStoredEntityInternal(uuid).ifPresent(compoundTag -> {
                    if (!compoundTag.isEmpty())
                        this.chunkTag.putEntity(uuid, compoundTag);
                });
            }
            this.entities.clear(); // clean-up entities that doesn't exist
            this.entities.addAll(entities); // these are the new entities in that chunk
        } else {
            for (UUID uuid : this.entities) {
                if (worldRegister.isEntityStoredInternal(uuid)) {
                    this.chunkTag.putEntity(uuid, worldRegister.unloadEntityInternal(uuid));
                }
            }
        }
        RegionFile regionTag = this.register.getRegionFile();
        if (!this.chunkTag.isEmpty(true))
            regionTag.putChunkCompound(this.uuid, this.chunkTag);
        else if (regionTag.containsChunkCompounds(uuid))
            regionTag.removeChunkCompound(uuid);
    }

    public boolean isBlockStored(Block block) {
        BlockUUID blockUUID = new BlockUUID(block);
        return this.chunkTag.containsBlock(blockUUID);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block);
        return Optional.ofNullable(this.chunkTag.getBlock(blockUUID));
    }

    public CompoundTag getStoredBlockUnsafe(Block block) {
        BlockUUID blockUUID = new BlockUUID(block);
        return this.chunkTag.getBlock(blockUUID);
    }

    public CompoundTag createStoredBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block);
        CompoundTag tag = new CompoundTag();
        this.chunkTag.putBlock(blockUUID, tag);
        return tag;
    }

    public CompoundTag getOrCreateBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block);
        if (this.chunkTag.containsBlock(blockUUID))
            return this.chunkTag.getBlock(blockUUID);
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

    public CompoundTag getStoredEntityUnsafe(UUID uuid) {
        return this.register.getStoredEntityUnsafe(uuid);
    }

    public CompoundTag createStoredEntity(UUID uuid) {
        this.refferenceEntity(uuid);
        return this.register.createStoredEntity(uuid);
    }

    public CompoundTag getOrCreateEntity(UUID uuid) {
        this.refferenceEntity(uuid);
        return this.register.getOrCreateEntity(uuid);
    }


    public void refferenceEntity(UUID uuid) {
        this.entities.add(uuid);
    }

    public RegionRegister getRegionRegister() {
        return this.register;
    }

    public ChunkCompoundTag getChunkTag() {
        synchronized (this.chunkTag) {
            return this.chunkTag;
        }
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
