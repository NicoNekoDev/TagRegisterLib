package ro.nicuch.tag.register;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.events.ChunkTagLoadEvent;
import ro.nicuch.tag.nbt.BlockCompoundTag;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.wrapper.BlockUUID;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ChunkRegister {
    private final RegionRegister register;
    private final Chunk chunk;
    private final BlockCompoundTag blocks;
    private final Set<UUID> entities = new HashSet<>();
    private final CompoundTag chunkTag;

    private final ChunkUUID uuid;

    public final ChunkUUID getChunkUUID() {
        return this.uuid;
    }

    public ChunkRegister(RegionRegister register, Chunk chunk) {
        this.uuid = new ChunkUUID(chunk.getX(), chunk.getZ());
        this.register = register;
        this.chunk = chunk;
        CompoundTag regionTag = register.getRegionTag();
        String chunkID = this.uuid.toString();
        if (regionTag.containsCompound(chunkID))
            this.chunkTag = regionTag.getCompound(chunkID);
        else {
            this.chunkTag = new CompoundTag();
        }
        CompoundTag entities;
        if (this.chunkTag.containsCompound("entities"))
            entities = this.chunkTag.getCompound("entities");
        else {
            entities = new CompoundTag();
        }
        if (this.chunkTag.containsBlockCompound("blocks"))
            blocks = this.chunkTag.getBlockCompound("blocks");
        else {
            blocks = new BlockCompoundTag();
        }
        for (String entityID : entities.keySet()) {
            UUID entityUUID = UUID.fromString(entityID);
            register.getWorldRegister().loadEntity(entityUUID, entities.getCompound(entityID));
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
        this.blocks.clear();
    }

    public void savePopulation(boolean checkEntities, Set<UUID> entitiesArray) {
        CompoundTag entities = new CompoundTag();
        WorldRegister worldRegister = this.register.getWorldRegister();
        if (checkEntities) {
            for (UUID entitiyUUID : entitiesArray)
                worldRegister.getStoredEntity(entitiyUUID).ifPresent(compoundTag -> {
                    if (!compoundTag.isEmpty())
                        entities.put(entitiyUUID.toString(), compoundTag);
                });
        } else {
            for (UUID uuid : this.entities) {
                if (worldRegister.isEntityStored(uuid)) {
                    entities.put(uuid.toString(), worldRegister.unloadEntity(uuid));
                }
            }
        }
        if (!entities.isEmpty())
            this.chunkTag.put("entities", entities);
        else if (this.chunkTag.containsCompound("entities"))
            this.chunkTag.remove("entities");
        if (!this.blocks.isEmpty())
            this.blocks.values().removeIf(CompoundTag::isEmpty);
        if (!this.blocks.isEmpty())
            this.chunkTag.put("blocks", this.blocks);
        else if (this.chunkTag.containsCompound("blocks"))
            this.chunkTag.remove("blocks");
        CompoundTag regionTag = this.register.getRegionTag();
        String uuid = this.uuid.toString();
        if (!this.chunkTag.isEmpty())
            regionTag.put(uuid, this.chunkTag);
        else if (regionTag.containsCompound(uuid))
            regionTag.remove(uuid);
    }

    public boolean isBlockStored(Block block) {
        BlockUUID blockUUID = new BlockUUID(block.getX(), block.getY(), block.getZ());
        return this.blocks.contains(blockUUID);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block.getX(), block.getY(), block.getZ());
        return Optional.ofNullable(this.blocks.get(blockUUID));
    }

    public CompoundTag createStoredBlock(Block block) {
        BlockUUID blockUUID = new BlockUUID(block.getX(), block.getY(), block.getZ());
        CompoundTag tag = new CompoundTag();
        this.blocks.put(blockUUID, tag);
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

    public CompoundTag getChunkTag() {
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
