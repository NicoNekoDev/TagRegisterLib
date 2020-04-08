package ro.nicuch.tag.register;

import com.mfk.lockfree.map.LockFreeMap;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.lwjnbtl.CompoundTag;
import ro.nicuch.lwjnbtl.TagIO;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.events.WorldTagLoadEvent;
import ro.nicuch.tag.fallback.CoruptedDataFallback;
import ro.nicuch.tag.fallback.CoruptedDataManager;
import ro.nicuch.tag.wrapper.RegionUUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class WorldRegister implements CoruptedDataFallback {
    private CompoundTag worldTag;
    private final File worldFile;
    private final File worldDataFolder;
    private final World world;
    private final LockFreeMap<RegionUUID, RegionRegister> regions = LockFreeMap.newMap(1);
    private final LockFreeMap<UUID, CompoundTag> entities = LockFreeMap.newMap(1);

    public WorldRegister(World world) {
        this.world = world;
        this.worldDataFolder = new File(world.getWorldFolder().getPath() + File.separator + "tags");
        this.worldDataFolder.mkdirs();
        this.worldFile = new File(world.getWorldFolder().getPath() + File.separator + "tag_level.dat");
        if (!this.worldFile.exists()) {
            try {
                this.worldFile.createNewFile();
                this.worldFile.setReadable(true, false);
                this.worldFile.setWritable(true, false);
                this.worldTag = new CompoundTag();
                this.writeWorldFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.readWorldFile();
        Bukkit.getScheduler().runTask(TagRegister.getPlugin(), () ->
                Bukkit.getPluginManager().callEvent(new WorldTagLoadEvent(this, this.worldTag)));
    }

    public File getDirectory() {
        return this.worldDataFolder;
    }

    public void readWorldFile() {
        try (FileInputStream fileInputStream = new FileInputStream(this.worldFile)) {
            this.worldTag = TagIO.readInputStream(fileInputStream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Reading) " + this.world.getName() + "'s level file is corupted.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeWorldFile() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.worldFile)) {
            TagIO.writeOutputStream(this.worldTag, fileOutputStream);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Writing) " + this.world.getName() + "'s level file is corupted.");
            CoruptedDataManager.fallbackOperation(this);
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
        RegionRegister region = new RegionRegister(this, chunk);
        this.regions.put(region.getRegionUUID(), region);
        return region;
    }

    public Optional<RegionRegister> getRegion(Chunk chunk) {
        return this.regions.get(RegionUUID.fromChunk(chunk));
    }

    public CompoundTag loadEntity(UUID uuid, CompoundTag tag) {
        this.entities.put(uuid, tag);
        return tag;
    }

    public CompoundTag createStoredEntity(UUID uuid) {
        return loadEntity(uuid, new CompoundTag());
    }

    public boolean isEntityStored(UUID uuid) {
        return this.entities.containsKey(uuid);
    }

    public CompoundTag unloadEntity(UUID uuid) {
        return this.entities.removeAndReturn(uuid);
    }

    public Optional<CompoundTag> getStoredEntity(UUID uuid) {
        return this.entities.get(uuid);
    }

    public void tryUnloading() {

        for (RegionUUID regionUUID : this.regions.keySet()) {
            RegionRegister regionRegister = this.regions.getUnsafe(regionUUID);
            if (regionRegister.canBeUnloaded()) {
                regionRegister.writeRegionFile();
                regionRegister.clearChunks();
                this.regions.remove(regionUUID);
            }
        }

        Set<UUID> worldEntities = new HashSet<>();
        for (Entity entity : this.world.getEntities())
            worldEntities.add(entity.getUniqueId());
        for (UUID uuid : this.entities.keySet()) {
            if (!worldEntities.contains(uuid))
                this.entities.remove(uuid);
        }
        worldEntities.clear(); // manual gc
    }

    public void saveRegions() {
        for (RegionRegister region : this.regions.values()) {
            region.saveChunks();
            region.writeRegionFile();
        }
        this.writeWorldFile();
    }

    public boolean isBlockStored(Block block) {
        return this.getRegion(block.getChunk()).orElseGet(() -> this.loadRegion(block.getChunk())).isBlockStored(block);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        return this.getRegion(block.getChunk()).orElseGet(() -> this.loadRegion(block.getChunk())).getStoredBlock(block);
    }

    public CompoundTag createStoredBlock(Block block) {
        return this.getRegion(block.getChunk()).orElseGet(() -> this.loadRegion(block.getChunk())).createStoredBlock(block);
    }

    public boolean isEntityStored(Entity entity) {
        Chunk realChunk = entity.getLocation().getChunk();
        RegionRegister region = this.getRegion(realChunk).orElseGet(() -> this.loadRegion(realChunk));
        if (region.isChunkNotLoaded(realChunk))
            region.loadChunk(realChunk);
        return this.isEntityStored(entity.getUniqueId());
    }

    public Optional<CompoundTag> getStoredEntity(Entity entity) {
        Chunk realChunk = entity.getLocation().getChunk();
        RegionRegister region = this.getRegion(realChunk).orElseGet(() -> this.loadRegion(realChunk));
        if (region.isChunkNotLoaded(realChunk))
            region.loadChunk(realChunk);
        return this.getStoredEntity(entity.getUniqueId());
    }

    public CompoundTag createStoredEntity(Entity entity) {
        Chunk realChunk = entity.getLocation().getChunk();
        RegionRegister region = this.getRegion(realChunk).orElseGet(() -> this.loadRegion(realChunk));
        if (region.isChunkNotLoaded(realChunk))
            region.loadChunk(realChunk);
        return this.createStoredEntity(entity.getUniqueId());
    }

    @Override
    public String getCoruptedDataId() {
        return this.world.getName() + "_world";
    }

    @Override
    public File getCoruptedDataFile() {
        return this.worldFile;
    }

    @Override
    public CompoundTag getCoruptedDataCompoundTag() {
        return this.worldTag;
    }

    public World getWorldInstance() {
        return this.world;
    }
}
