package ro.nicuch.tag.register;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.springframework.util.ConcurrentReferenceHashMap;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.TagIO;
import ro.nicuch.tag.nbt.TagType;
import ro.nicuch.tag.wrapper.RegionUUID;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class WorldRegister {
    private CompoundTag worldTag;
    private final File worldFile;
    private final File worldDataFolder;
    private final World world;
    private final ConcurrentMap<RegionUUID, RegionRegister> regions = new ConcurrentHashMap<>(16);
    private final ConcurrentReferenceHashMap<RegionUUID, ReentrantLock> regionsLock = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK);
    private final ConcurrentMap<UUID, CompoundTag> entities = new ConcurrentHashMap<>(16);

    public WorldRegister(World world) {
        this.world = world;
        this.worldDataFolder = new File(world.getWorldFolder().getPath() + File.separator + "tags");
        this.worldDataFolder.mkdirs();
        this.worldFile = new File(world.getWorldFolder().getPath() + File.separator + "tag_level.dat");
        if (!this.worldFile.exists()) {
            this.worldTag = new CompoundTag();
        } else {
            try {
                this.worldTag = (CompoundTag) TagIO.readFile(this.worldFile, TagType.COMPOUND);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                System.out.println("(Reading) " + this.world.getName() + "'s level file is corupted.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ReentrantLock getRegionLock(RegionUUID regionUUID) {
        if (this.regionsLock.containsKey(regionUUID))
            return this.regionsLock.get(regionUUID);
        ReentrantLock lock = new ReentrantLock();
        regionsLock.put(regionUUID, lock);
        return lock;
    }

    public File getDirectory() {
        return this.worldDataFolder;
    }

    protected void writeWorldFile() {
        if (this.worldTag.isEmpty()) {
            if (this.worldFile.exists())
                worldFile.delete();
            return;
        }
        if (!this.worldFile.exists()) {
            try {
                this.worldFile.createNewFile();
                this.worldFile.setReadable(true, false);
                this.worldFile.setWritable(true, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            TagIO.writeFile(this.worldTag, this.worldFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Writing) " + this.world.getName() + "'s level file is corupted.");
        }
    }

    public CompoundTag getWorldTag() {
        return this.worldTag;
    }

    public void setWorldTag(CompoundTag tag) {
        this.worldTag = tag;
    }

    public boolean isRegionLoaded(Chunk chunk) {
        return this.regions.containsKey(RegionUUID.fromChunk(chunk));
    }

    public RegionRegister loadRegion(Chunk chunk) {
        RegionUUID regionUUID = RegionUUID.fromChunk(chunk);
        ReentrantLock lock = this.getRegionLock(regionUUID);
        lock.lock();
        try {
            RegionRegister region = new RegionRegister(this, regionUUID);
            this.regions.put(regionUUID, region);
            return region;
        } finally {
            lock.unlock();
        }
    }

    public Optional<RegionRegister> getRegion(Chunk chunk) {
        RegionUUID regionUUID = RegionUUID.fromChunk(chunk);
        ReentrantLock lock = this.getRegionLock(regionUUID);
        lock.lock();
        try {
            return Optional.ofNullable(this.regions.get(regionUUID));
        } finally {
            lock.unlock();
        }
    }

    public RegionRegister getRegionUnsafe(Chunk chunk) {
        RegionUUID regionUUID = RegionUUID.fromChunk(chunk);
        ReentrantLock lock = this.getRegionLock(regionUUID);
        lock.lock();
        try {
            return this.regions.get(regionUUID);
        } finally {
            lock.unlock();
        }
    }

    public RegionRegister getOrLoadRegion(Chunk chunk) {
        RegionUUID regionUUID = RegionUUID.fromChunk(chunk);
        ReentrantLock lock = this.getRegionLock(regionUUID);
        lock.lock();
        try {
            if (this.regions.containsKey(regionUUID))
                return this.regions.get(regionUUID);
            RegionRegister region = new RegionRegister(this, regionUUID);
            this.regions.put(regionUUID, region);
            return region;
        } finally {
            lock.unlock();
        }
    }

    public CompoundTag loadEntityInternal(UUID uuid, CompoundTag tag) {
        this.entities.put(uuid, tag);
        return tag;
    }

    public CompoundTag createStoredEntityInternal(UUID uuid) {
        return loadEntityInternal(uuid, new CompoundTag());
    }

    public boolean isEntityStoredInternal(UUID uuid) {
        return this.entities.containsKey(uuid);
    }

    public CompoundTag getOrCreateEntityInternal(UUID uuid) {
        if (this.entities.containsKey(uuid)) {
            return this.entities.get(uuid);
        }
        CompoundTag entityTag = new CompoundTag();
        this.entities.put(uuid, entityTag);
        return entityTag;
    }

    public CompoundTag unloadEntityInternal(UUID uuid) {
        return this.entities.remove(uuid);
    }

    public Optional<CompoundTag> getStoredEntityInternal(UUID uuid) {
        return Optional.ofNullable(this.entities.get(uuid));
    }

    public CompoundTag getStoredEntityInternalUnsafe(UUID uuid) {
        return this.entities.get(uuid);
    }

    public void tryUnloading() {
        Iterator<Map.Entry<RegionUUID, RegionRegister>> regionsIterator = this.regions.entrySet().iterator();
        while (regionsIterator.hasNext()) {
            Map.Entry<RegionUUID, RegionRegister> entry = regionsIterator.next();
            RegionUUID regionUUID = entry.getKey();
            ReentrantLock lock = this.getRegionLock(regionUUID);
            lock.lock();
            try {
                RegionRegister regionRegister = entry.getValue();
                if (regionRegister.canBeUnloaded()) {
                    regionRegister.getRegionFile().commit(); // commit cache
                    regionRegister.getRegionFile().close(); // close cache
                    regionsIterator.remove();
                }
            } finally {
                lock.unlock();
            }
        }
        /*Iterator<RegionRegister> regionIterator = this.regions.values().iterator();
        while (regionIterator.hasNext()) {
            RegionRegister regionRegister = regionIterator.next();
            if (regionRegister.canBeUnloaded()) {
                regionRegister.getRegionTag().commit(); // commit cache
                regionRegister.getRegionTag().close(); // close cache
                regionIterator.remove();
            }
        }*/
        Set<UUID> worldEntities = this.world.getEntities().stream().map(Entity::getUniqueId).collect(Collectors.toSet());
        this.entities.keySet().removeIf(uuid -> !worldEntities.contains(uuid));
        worldEntities.clear(); // manual gc
    }

    public void saveRegions() {
        for (Map.Entry<RegionUUID, RegionRegister> entry : this.regions.entrySet()) {
            RegionUUID regionUUID = entry.getKey();
            ReentrantLock lock = this.getRegionLock(regionUUID);
            lock.lock();
            try {
                RegionRegister regionRegister = entry.getValue();
                regionRegister.saveChunks();
                regionRegister.getRegionFile().commit(); //commit to files
            } finally {
                lock.unlock();
            }
        }
        /*for (RegionRegister region : this.regions.values()) {
            region.saveChunks();
            region.getRegionTag().commit(); //commit to files
        }*/
        this.writeWorldFile();
    }

    public boolean isBlockStored(Block block) {
        return this.getOrLoadRegion(block.getChunk()).isBlockStored(block);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        return this.getOrLoadRegion(block.getChunk()).getStoredBlock(block);
    }

    public CompoundTag createStoredBlock(Block block) {
        return this.getOrLoadRegion(block.getChunk()).createStoredBlock(block);
    }

    public CompoundTag getOrCreateBlock(Block block) {
        return this.getOrLoadRegion(block.getChunk()).getOrCreateBlock(block);
    }

    public boolean isEntityStored(Entity entity) {
        Chunk chunk = entity.getLocation().getChunk();
        return this.getOrLoadRegion(chunk).getOrLoadChunk(chunk).isEntityStored(entity.getUniqueId());
    }

    public Optional<CompoundTag> getStoredEntity(Entity entity) {
        Chunk chunk = entity.getLocation().getChunk();
        return this.getOrLoadRegion(chunk).getOrLoadChunk(chunk).getStoredEntity(entity.getUniqueId());
    }

    public CompoundTag createStoredEntity(Entity entity) {
        Chunk chunk = entity.getLocation().getChunk();
        return this.getOrLoadRegion(chunk).getOrLoadChunk(chunk).createStoredEntity(entity.getUniqueId());
    }

    public CompoundTag getOrCreateEntity(Entity entity) {
        Chunk chunk = entity.getLocation().getChunk();
        return this.getOrLoadRegion(chunk).getOrLoadChunk(chunk).getOrCreateEntity(entity.getUniqueId());
    }

    public CompoundTag getStoredEntityUnsafe(Entity entity) {
        Chunk chunk = entity.getLocation().getChunk();
        return this.getOrLoadRegion(chunk).getOrLoadChunk(chunk).getStoredEntityUnsafe(entity.getUniqueId());
    }

    public CompoundTag getStoredBlockUnsafe(Block block) {
        return this.getOrLoadRegion(block.getChunk()).getStoredBlockUnsafe(block);
    }

    public World getWorldInstance() {
        return this.world;
    }

    @Override
    public String toString() {
        return "WorldRegister{" +
                "w: " + this.world.getName() +
                "}";
    }
}
