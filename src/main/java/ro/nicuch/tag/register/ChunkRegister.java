package ro.nicuch.tag.register;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import ro.nicuch.lwjnbtl.CompoundTag;
import ro.nicuch.lwjnbtl.TagType;
import ro.nicuch.tag.events.ChunkTagLoadEvent;
import ro.nicuch.tag.wrapper.BlockUUID;
import ro.nicuch.tag.wrapper.ChunkUUID;

public class ChunkRegister {
    private final RegionRegister register;
    private final Chunk chunk;
    private final ConcurrentMap<BlockUUID, CompoundTag> blocks = new ConcurrentHashMap<>();
    private final Set<UUID> entities = new HashSet<>();
    private final CompoundTag chunkTag;

    private final ChunkUUID uuid;

    public final ChunkUUID getChunkUUID() {
        return this.uuid;
    }

    public ChunkRegister(RegionRegister register, Chunk chunk) {
        this.uuid = new ChunkUUID(chunk);

        this.register = register;
        this.chunk = chunk;
        CompoundTag regionTag = register.getRegionTag();
        String chunkID = this.uuid.toString();
        if (regionTag.contains(chunkID, TagType.COMPOUND))
            this.chunkTag = (CompoundTag) regionTag.get(chunkID);
        else {
            this.chunkTag = new CompoundTag();
            regionTag.put(chunkID, chunkTag);
        }
        CompoundTag chunkTag = this.chunkTag;
        CompoundTag entities;
        if (chunkTag.contains("entities", TagType.COMPOUND))
            entities = chunkTag.getCompound("entities");
        else {
            entities = new CompoundTag();
            chunkTag.put("entities", entities);
        }
        CompoundTag blocks;
        if (chunkTag.contains("blocks", TagType.COMPOUND))
            blocks = chunkTag.getCompound("blocks");
        else {
            blocks = new CompoundTag();
            chunkTag.put("blocks", blocks);
        }
        for (String entityID : entities.keySet()) {
            UUID entityUUID = UUID.fromString(entityID);
            register.getWorldRegister().loadEntity(entityUUID, entities.getCompound(entityID));
            this.entities.add(entityUUID);
        }
        for (String blockID : blocks.keySet()) {
            this.blocks.put(BlockUUID.fromString(blockID), blocks.getCompound(blockID));
        }
        Bukkit.getPluginManager().callEvent(new ChunkTagLoadEvent(this, this.chunkTag));
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

    public void unload(boolean checkEntities) {
        this.savePopulation(checkEntities);
        this.entities.clear();
        this.blocks.clear();
    }

    public void savePopulation(boolean checkEntities) {
        CompoundTag entities = new CompoundTag();
        WorldRegister worldRegister = this.register.getWorldRegister();
        if (checkEntities) {
            for (Entity entity : this.chunk.getEntities())
                worldRegister.getStoredEntity(entity.getUniqueId()).ifPresent(compoundTag -> entities.put(entity.getUniqueId().toString(), compoundTag));
        } else {
            for (UUID uuid : this.entities) {
                if (worldRegister.isEntityStored(uuid)) {
                    entities.put(uuid.toString(), worldRegister.unloadEntity(uuid));
                }
            }
        }
        CompoundTag blocks = new CompoundTag();
        for (BlockUUID blockUUID : this.blocks.keySet()) {
            blocks.put(blockUUID.toString(), this.blocks.get(blockUUID));
        }
        this.chunkTag.put("entities", entities);
        this.chunkTag.put("blocks", blocks);
        this.register.getRegionTag().put(this.uuid.toString(), this.chunkTag);
    }

    public boolean isBlockStored(Block block) {
        return this.blocks.containsKey(BlockUUID.fromBlock(block));
    }

    public synchronized Optional<CompoundTag> getStoredBlock(Block block) {
        return Optional.ofNullable(this.blocks.get(BlockUUID.fromBlock(block)));
    }

    public synchronized CompoundTag createStoredBlock(Block block) {
        BlockUUID blockUUID = BlockUUID.fromBlock(block);
        CompoundTag tag = new CompoundTag();
        this.blocks.putIfAbsent(blockUUID, tag);
        return tag;
    }

    public boolean isEntityStored(Entity entity) {
        return this.register.isEntityStored(entity);
    }

    public Optional<CompoundTag> getStoredEntity(Entity entity) {
        return this.register.getStoredEntity(entity);
    }

    public CompoundTag createStoredEntity(Entity entity) {
        return this.register.createStoredEntity(entity);
    }

    public RegionRegister getRegionRegister() {
        return this.register;
    }

    public CompoundTag getChunkTag() {
        return this.chunkTag;
    }


    //Needed for GC
    @Override
    public int hashCode() {
        return uuid.hashCode() * 487;
    }

    //Needed for GC
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkRegister))
            return false;
        ChunkRegister chunkRegister = (ChunkRegister) obj;
        return this.getChunkUUID().equals(chunkRegister.getChunkUUID());
    }
}
