package ro.nicuch.tag.register;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import ro.nicuch.lwjnbtl.CompoundTag;
import ro.nicuch.lwjnbtl.TagIO;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.events.RegionTagLoadEvent;
import ro.nicuch.tag.fallback.CoruptedDataFallback;
import ro.nicuch.tag.fallback.CoruptedDataManager;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.RegionUUID;

public class RegionRegister implements CoruptedDataFallback {
    private final WorldRegister register;
    private final ConcurrentMap<ChunkUUID, ChunkRegister> chunks = new ConcurrentHashMap<>();
    private final int x;
    private final int z;
    private final File regionFile;
    private CompoundTag regionTag;

    private final RegionUUID uuid;

    public final RegionUUID getRegionUUID() {
        return this.uuid;
    }

    public RegionRegister(WorldRegister register, Chunk chunk) {
        this(register, Math.floorDiv(chunk.getX(), 32), Math.floorDiv(chunk.getZ(), 32));
    }

    public RegionRegister(WorldRegister register, int x, int z) {
        this.uuid = new RegionUUID(x, z);

        this.register = register;
        this.x = x;
        this.z = z;
        this.regionFile = new File(register.getDirectory().getPath() + File.separator + "r." + x + "." + z + ".dat");
        if (!this.regionFile.exists()) {
            try {
                this.regionFile.createNewFile();
                this.regionFile.setReadable(true, false);
                this.regionFile.setWritable(true, false);
                this.regionTag = new CompoundTag();
                this.writeRegionFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.readRegionFile();
        Bukkit.getScheduler().runTask(TagRegister.getPlugin(), () ->
                Bukkit.getPluginManager().callEvent(new RegionTagLoadEvent(this, this.regionTag)));
    }

    public WorldRegister getWorldRegister() {
        return this.register;
    }

    public CompoundTag getRegionTag() {
        return this.regionTag;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public void readRegionFile() {
        try {
            this.regionTag = TagIO.readInputStream(new FileInputStream(this.regionFile));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Reading) This region is corupted. -> r." + x + "." + z + ".dat!!");
        }
    }

    public void writeRegionFile() {
        try {
            TagIO.writeOutputStream(this.regionTag, new FileOutputStream(this.regionFile));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Writing) This region file is corupted. -> r." + x + "." + z + ".dat");
            CoruptedDataManager.fallbackOperation(this);
        }
    }

    public boolean isChunkNotLoaded(Chunk chunk) {
        //Allways inveted
        return !this.chunks.containsKey(ChunkUUID.fromChunk(chunk));
    }

    public boolean isChunkNotLoaded(ChunkUUID chunkUUID) {
        return this.chunks.containsKey(chunkUUID);
    }

    public ChunkRegister loadChunk(Chunk chunk) {
        ChunkRegister cr = new ChunkRegister(this, chunk);
        this.chunks.putIfAbsent(ChunkUUID.fromChunk(chunk), cr);
        return cr;
    }

    public Optional<ChunkRegister> getChunk(Chunk chunk) {
        return this.getChunk(ChunkUUID.fromChunk(chunk));
    }

    public Optional<ChunkRegister> getChunk(ChunkUUID chunkUUID) {
        return Optional.ofNullable(this.chunks.get(chunkUUID));
    }

    public ChunkRegister removeChunk(Chunk chunk) {
        return this.chunks.remove(ChunkUUID.fromChunk(chunk));
    }

    public void unloadChunk(Chunk chunk, Set<UUID> entitiesArray) {
        ChunkUUID chunkUUID = ChunkUUID.fromChunk(chunk);
        Optional<ChunkRegister> chunkRegister = this.getChunk(chunkUUID);
        if (chunkRegister.isPresent()) {
            chunkRegister.get().unload(true, entitiesArray);
            this.removeChunk(chunk);
        }
    }

    public boolean canBeUnloaded() {
        for (ChunkRegister chunk : this.chunks.values()) {
            Chunk realChunk = chunk.getChunk();
            if (!realChunk.isLoaded()) {
                this.removeChunk(realChunk);
                chunk.unload(false, null);
            }
        }
        return this.chunks.isEmpty();
    }

    public void saveChunks() {
        for (ChunkRegister chunk : this.chunks.values())
            if (chunk.getChunk().isLoaded()) {
                Set<UUID> entitiesUUID = new HashSet<>();
                for (Entity entity : chunk.getChunk().getEntities())
                    entitiesUUID.add(entity.getUniqueId());
                chunk.savePopulation(true, entitiesUUID);
            } else
                chunk.savePopulation(false, null);
    }

    public boolean isBlockStored(Block block) {
        return this.getChunk(block.getChunk()).orElseGet(() -> this.loadChunk(block.getChunk())).isBlockStored(block);
    }

    public Optional<CompoundTag> getStoredBlock(Block block) {
        return this.getChunk(block.getChunk()).orElseGet(() -> this.loadChunk(block.getChunk())).getStoredBlock(block);
    }

    public CompoundTag createStoredBlock(Block block) {
        return this.getChunk(block.getChunk()).orElseGet(() -> this.loadChunk(block.getChunk())).createStoredBlock(block);
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

    public RegionRegister setRegionTag(CompoundTag tag) {
        this.regionTag = tag;
        return this;
    }

    @Override
    public String getCoruptedDataId() {
        return "r." + x + "." + z + "_region";
    }

    @Override
    public File getCoruptedDataFile() {
        return this.regionFile;
    }

    @Override
    public CompoundTag getCoruptedDataCompoundTag() {
        return this.regionTag;
    }

    //Needed for GC
    @Override
    public int hashCode() {
        return uuid.hashCode() * 569;
    }

    //Needed for GC
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RegionRegister))
            return false;
        RegionRegister regionRegister = (RegionRegister) obj;
        return this.getRegionUUID().equals(regionRegister.getRegionUUID());
    }
}
