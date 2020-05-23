package ro.nicuch.tag.register;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.events.WorldTagLoadEvent;
import ro.nicuch.tag.fallback.CoruptedDataFallback;
import ro.nicuch.tag.fallback.CoruptedDataManager;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.Tag;
import ro.nicuch.tag.nbt.TagIO;
import ro.nicuch.tag.nbt.TagType;
import ro.nicuch.tag.wrapper.RegionUUID;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class WorldRegister implements CoruptedDataFallback {
    private CompoundTag worldTag;
    private final File worldFile;
    private final File worldDataFolder;
    private final World world;
    private final Map<RegionUUID, RegionRegister> regions = new HashMap<>();
    private final Map<UUID, CompoundTag> entities = new HashMap<>();

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
        Bukkit.getScheduler().runTask(TagRegister.getPlugin(), () ->
                Bukkit.getPluginManager().callEvent(new WorldTagLoadEvent(this, this.worldTag)));
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
        synchronized (this.regions) {
            return this.regions.containsKey(RegionUUID.fromChunk(chunk));
        }
    }

    public RegionRegister loadRegion(Chunk chunk) {
        synchronized (this.regions) {
            RegionRegister region = new RegionRegister(this, chunk);
            this.regions.put(region.getRegionUUID(), region);
            return region;
        }
    }

    public Optional<RegionRegister> getRegion(Chunk chunk) {
        synchronized (this.regions) {
            return Optional.ofNullable(this.regions.get(RegionUUID.fromChunk(chunk)));
        }
    }

    public CompoundTag loadEntityInternal(UUID uuid, CompoundTag tag) {
        synchronized (this.entities) {
            this.entities.put(uuid, tag);
            return tag;
        }
    }

    public CompoundTag createStoredEntityInternal(UUID uuid) {
        return loadEntityInternal(uuid, new CompoundTag());
    }

    public boolean isEntityStoredInternal(UUID uuid) {
        synchronized (this.entities) {
            return this.entities.containsKey(uuid);
        }
    }

    public CompoundTag unloadEntityInternal(UUID uuid) {
        synchronized (this.entities) {
            return this.entities.remove(uuid);
        }
    }

    public Optional<CompoundTag> getStoredEntityInternal(UUID uuid) {
        synchronized (this.entities) {
            return Optional.ofNullable(this.entities.get(uuid));
        }
    }

    public void tryUnloading() {
        synchronized (this.regions) {
            Iterator<RegionRegister> regionIterator = this.regions.values().iterator();
            while (regionIterator.hasNext()) {
                RegionRegister regionRegister = regionIterator.next();
                if (regionRegister.canBeUnloaded()) {
                    regionRegister.writeRegionFile();
                    regionRegister.getRegionTag().close(); // close cache
                    regionIterator.remove();
                }
            }
        }
        synchronized (this.entities) {
            Set<UUID> worldEntities = this.world.getEntities().stream().map(Entity::getUniqueId).collect(Collectors.toSet());
            for (UUID uuid : this.entities.keySet()) {
                if (!worldEntities.contains(uuid))
                    this.entities.remove(uuid);
            }
            worldEntities.clear(); // manual gc
        }
    }

    public void saveRegions() {
        synchronized (this.regions) {
            for (RegionRegister region : this.regions.values()) {
                region.saveChunks();
                region.writeRegionFile();
            }
            this.writeWorldFile();
        }
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
        ChunkRegister chunk = region.getChunk(realChunk).orElseGet(() -> region.loadChunk(realChunk));
        return chunk.isEntityStored(entity.getUniqueId());
    }

    public Optional<CompoundTag> getStoredEntity(Entity entity) {
        Chunk realChunk = entity.getLocation().getChunk();
        RegionRegister region = this.getRegion(realChunk).orElseGet(() -> this.loadRegion(realChunk));
        ChunkRegister chunk = region.getChunk(realChunk).orElseGet(() -> region.loadChunk(realChunk));
        return chunk.getStoredEntity(entity.getUniqueId());
    }

    public CompoundTag createStoredEntity(Entity entity) {
        Chunk realChunk = entity.getLocation().getChunk();
        RegionRegister region = this.getRegion(realChunk).orElseGet(() -> this.loadRegion(realChunk));
        ChunkRegister chunk = region.getChunk(realChunk).orElseGet(() -> region.loadChunk(realChunk));
        return chunk.createStoredEntity(entity.getUniqueId());
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
    public Tag getCoruptedDataCompoundTag() {
        return this.worldTag;
    }

    @Override
    public String getWorldName() {
        return this.world.getName();
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
