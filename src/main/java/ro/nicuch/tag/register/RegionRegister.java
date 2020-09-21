package ro.nicuch.tag.register;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.region.RegionFile;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.RegionUUID;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class RegionRegister {
    private final WorldRegister register;
    private final ConcurrentMap<ChunkUUID, ChunkRegister> chunks = new ConcurrentHashMap<>();
    private final Map<ChunkUUID, ReentrantLock> chunksLock = Collections.synchronizedMap(new WeakHashMap<>());
    private final int x;
    private final int z;
    private RegionFile regionFile;
    private final RegionUUID uuid;

    private ReentrantLock getChunkLock(ChunkUUID chunkUUID) {
        if (this.chunksLock.containsKey(chunkUUID))
            return this.chunksLock.get(chunkUUID);
        ReentrantLock lock = new ReentrantLock();
        chunksLock.put(chunkUUID, lock);
        return lock;
    }

    public final RegionUUID getRegionUUID() {
        return this.uuid;
    }

    public RegionRegister(WorldRegister register, Chunk chunk) {
        this(register, Math.floorDiv(chunk.getX(), 32), Math.floorDiv(chunk.getZ(), 32));
    }

    public RegionRegister(WorldRegister register, RegionUUID uuid) {
        this.uuid = uuid;
        this.register = register;
        this.x = uuid.getX();
        this.z = uuid.getZ();
        File regionFile = new File(register.getDirectory().getPath() + File.separator + "r." + x + "." + z + ".dat");
        try {
            this.regionFile = new RegionFile(regionFile, this.x, this.z);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("(Reading) This region is corupted. -> r." + x + "." + z + ".dat!!");
        }
    }

    public RegionRegister(WorldRegister register, int x, int z) {
        this(register, new RegionUUID(x, z));
    }

    public WorldRegister getWorldRegister() {
        return this.register;
    }

    public RegionFile getRegionFile() {
        return this.regionFile;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public boolean isChunkLoaded(Chunk chunk) {
        ChunkUUID chunkUUID = new ChunkUUID(chunk);
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            return this.chunks.containsKey(chunkUUID);
        } finally {
            lock.unlock();
        }
    }

    public boolean isChunkLoaded(ChunkUUID chunkUUID) {
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            return this.chunks.containsKey(chunkUUID);
        } finally {
            lock.unlock();
        }
    }

    public ChunkRegister getOrLoadChunk(Chunk chunk) {
        ChunkUUID chunkUUID = new ChunkUUID(chunk);
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            if (this.chunks.containsKey(chunkUUID))
                return this.chunks.get(chunkUUID);
            ChunkRegister chunkRegister = new ChunkRegister(this, chunk, chunkUUID);
            this.chunks.put(chunkUUID, chunkRegister);
            return chunkRegister;
        } finally {
            lock.unlock();
        }
    }

    public ChunkRegister loadChunk(Chunk chunk) {
        ChunkUUID chunkUUID = new ChunkUUID(chunk);
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            ChunkRegister chunkRegister = new ChunkRegister(this, chunk, chunkUUID);
            this.chunks.put(chunkUUID, chunkRegister);
            return chunkRegister;
        } finally {
            lock.unlock();
        }
    }

    public Optional<ChunkRegister> getChunk(Chunk chunk) {
        ChunkUUID chunkUUID = new ChunkUUID(chunk);
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            return Optional.ofNullable(this.chunks.get(chunkUUID));
        } finally {
            lock.unlock();
        }
    }

    public Optional<ChunkRegister> getChunk(ChunkUUID chunkUUID) {
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            return Optional.ofNullable(this.chunks.get(chunkUUID));
        } finally {
            lock.unlock();
        }
    }

    public ChunkRegister removeChunk(Chunk chunk) {
        ChunkUUID chunkUUID = new ChunkUUID(chunk);
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            return this.chunks.remove(chunkUUID);
        } finally {
            lock.unlock();
        }
    }

    public ChunkRegister unloadChunk(Chunk chunk, Set<UUID> entitiesArray) {
        ChunkUUID chunkUUID = new ChunkUUID(chunk);
        ReentrantLock lock = this.getChunkLock(chunkUUID);
        lock.lock();
        try {
            ChunkRegister chunkRegister = this.chunks.get(chunkUUID);
            chunkRegister.unload(true, entitiesArray);
            this.chunks.remove(chunkUUID);
            return chunkRegister;
        } finally {
            lock.unlock();
        }
    }

    public boolean canBeUnloaded() {
        Iterator<Map.Entry<ChunkUUID, ChunkRegister>> chunksIterator = this.chunks.entrySet().iterator();
        while (chunksIterator.hasNext()) {
            Map.Entry<ChunkUUID, ChunkRegister> entry = chunksIterator.next();
            ChunkUUID chunkUUID = entry.getKey();
            ReentrantLock lock = this.getChunkLock(chunkUUID);
            lock.lock();
            try {
                ChunkRegister chunkRegister = entry.getValue();
                if (!chunkRegister.getChunk().isLoaded()) {
                    chunkRegister.unload(false, null);
                    chunksIterator.remove();
                }
            } finally {
                lock.unlock();
            }
        }
        /*Iterator<ChunkRegister> chunkIterator = this.chunks.values().iterator();
        while (chunkIterator.hasNext()) {
            ChunkRegister chunkRegister = chunkIterator.next();
            if (!chunkRegister.getChunk().isLoaded()) {
                chunkRegister.unload(false, null);
                chunkIterator.remove();
            }
        }*/
        return this.chunks.isEmpty();
    }

    public void saveChunks() {
        for (Map.Entry<ChunkUUID, ChunkRegister> entry : this.chunks.entrySet()) {
            ChunkUUID chunkUUID = entry.getKey();
            ReentrantLock lock = this.getChunkLock(chunkUUID);
            lock.lock();
            try {
                ChunkRegister chunkRegister = entry.getValue();
                if (chunkRegister.getChunk().isLoaded()) {
                    chunkRegister.savePopulation(true, Arrays.stream(chunkRegister.getChunk().getEntities()).map(Entity::getUniqueId).collect(Collectors.toSet()));
                } else
                    chunkRegister.savePopulation(false, null);
            } finally {
                lock.unlock();
            }
        }
        /*for (ChunkRegister chunk : this.chunks.values())
            if (chunk.getChunk().isLoaded()) {
                chunk.savePopulation(true, Arrays.stream(chunk.getChunk().getEntities()).map(Entity::getUniqueId).collect(Collectors.toSet()));
            } else
                chunk.savePopulation(false, null);*/
    }

    public boolean isBlockStored(Block block) {
        return this.getOrLoadChunk(block.getChunk()).isBlockStored(block);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        return this.getOrLoadChunk(block.getChunk()).getStoredBlock(block);
    }

    public CompoundTag getStoredBlockUnsafe(Block block) {
        return this.getOrLoadChunk(block.getChunk()).getStoredBlockUnsafe(block);
    }

    public CompoundTag createStoredBlock(Block block) {
        return this.getOrLoadChunk(block.getChunk()).createStoredBlock(block);
    }

    public CompoundTag getOrCreateBlock(Block block) {
        return this.getOrLoadChunk(block.getChunk()).getOrCreateBlock(block);
    }

    public boolean isEntityStored(Entity entity) {
        return this.isEntityStored(entity.getUniqueId());
    }

    public boolean isEntityStored(UUID uuid) {
        return this.register.isEntityStoredInternal(uuid);
    }

    public Optional<CompoundTag> getStoredEntity(Entity entity) {
        return this.getStoredEntity(entity.getUniqueId());
    }

    public CompoundTag createStoredEntity(Entity entity) {
        return this.createStoredEntity(entity.getUniqueId());
    }

    public Optional<CompoundTag> getStoredEntity(UUID uuid) {
        return this.register.getStoredEntityInternal(uuid);
    }

    public CompoundTag getStoredEntityUnsafe(UUID uuid) {
        return this.register.getStoredEntityInternalUnsafe(uuid);
    }

    public CompoundTag createStoredEntity(UUID uuid) {
        return this.register.createStoredEntityInternal(uuid);
    }

    public CompoundTag getOrCreateEntity(UUID uuid) {
        return this.register.getOrCreateEntityInternal(uuid);
    }

    @Override
    public String toString() {
        return "RegionRegister{" +
                "x: " + this.uuid.getX() +
                ", y: " + this.uuid.getZ() +
                ", w: " + this.register.getWorldInstance().getName() +
                "}";
    }
}
