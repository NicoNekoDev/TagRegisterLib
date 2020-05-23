package ro.nicuch.tag.nbt.reg;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.TagRegisterSerializer;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.Tag;
import ro.nicuch.tag.nbt.TagType;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.*;
import java.util.*;

/**
 * A compound tag.
 */
public final class RegionCompoundTag implements Tag, Closeable {
    /**
     * The maximum depth.
     */
    public static final int MAX_DEPTH = 512;
    /**
     * The map of tags.
     */
    private Map<ChunkUUID, ChunkCompoundTag> chunks;
    private DB mapDB;
    private boolean caching;

    private final CompoundTag regionTag = new CompoundTag();

    public RegionCompoundTag() {
        this(false);
    }

    public RegionCompoundTag(boolean enableFileCaching) {
        if (enableFileCaching) {
            File cacheFile = new File(TagRegister.getPlugin().getDataFolder() + File.separator + "cache" + File.separator + UUID.randomUUID().toString() + ".cache");
            if (cacheFile.exists())
                cacheFile.delete();
            this.mapDB = DBMaker.fileDB(cacheFile).closeOnJvmShutdown().fileDeleteAfterClose().transactionEnable().make();
            this.caching = true;
        } else {
            this.caching = false;
            this.mapDB = DBMaker.memoryDB().closeOnJvmShutdown().transactionEnable().make();
        }
        this.chunks = this.mapDB.hashMap("chunks").keySerializer(TagRegisterSerializer.CHUNK_SERIALIZER).valueSerializer(TagRegisterSerializer.CHUNK_COMPOUND_TAG_SERIALIZER).createOrOpen();
    }

    public boolean isCaching() {
        return this.caching;
    }

    public void changeToCache() {
        Map<ChunkUUID, ChunkCompoundTag> copy = new HashMap<>(this.chunks);
        this.mapDB.close(); //close current
        File cacheFile = new File(TagRegister.getPlugin().getDataFolder() + File.separator + "cache" + File.separator + UUID.randomUUID().toString() + ".cache");
        if (cacheFile.exists())
            cacheFile.delete();
        this.mapDB = DBMaker.fileDB(cacheFile).closeOnJvmShutdown().fileDeleteAfterClose().transactionEnable().make();
        this.chunks = this.mapDB.hashMap("chunks").keySerializer(TagRegisterSerializer.CHUNK_SERIALIZER).valueSerializer(TagRegisterSerializer.CHUNK_COMPOUND_TAG_SERIALIZER).createOrOpen();
        this.chunks.putAll(copy);
        this.caching = true;
    }

    public void changeToMemory() {
        Map<ChunkUUID, ChunkCompoundTag> copy = new HashMap<>(this.chunks);
        this.mapDB.close(); //close current
        this.mapDB = DBMaker.memoryDB().closeOnJvmShutdown().transactionEnable().make();
        this.chunks = this.mapDB.hashMap("chunks").keySerializer(TagRegisterSerializer.CHUNK_SERIALIZER).valueSerializer(TagRegisterSerializer.CHUNK_COMPOUND_TAG_SERIALIZER).createOrOpen();
        this.chunks.putAll(copy);
        this.caching = false;
    }

    public boolean canChangeToCache() {
        int n = this.regionTag.size();
        if (n >= 63)
            return true;
        for (ChunkCompoundTag chunkCompoundTag : this.chunks.values()) {
            n += chunkCompoundTag.sizeBlocks() + chunkCompoundTag.sizeEntities();
            if (n >= 63)
                return true;
        }
        return false;
    }

    public boolean isEmpty(boolean removeEmpty) {
        if (removeEmpty)
            this.chunks.values().removeIf(compound -> compound.isEmpty(false));
        return this.chunks.isEmpty() && this.regionTag.isEmpty();
    }

    public CompoundTag getRegionCompound() {
        return this.regionTag;
    }

    /**
     * Clear the tag.
     */
    public void clearChunkCompounds() {
        this.chunks.clear();
        if (this.caching)
            changeToMemory();
    }

    /**
     * Gets a tag by its key.
     *
     * @param key the key
     * @return the tag, or {@code null}
     */
    public ChunkCompoundTag getChunkCompound(final ChunkUUID key) {
        return this.chunks.get(key);
    }

    /**
     * Inserts a tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public ChunkCompoundTag putChunkCompound(final ChunkUUID key, final ChunkCompoundTag tag) {
        ChunkCompoundTag compoundTag = this.chunks.put(key, tag);
        if (this.caching) {
            if (!this.canChangeToCache())
                this.changeToMemory();
        } else {
            if (this.canChangeToCache())
                this.changeToCache();
        }
        return compoundTag;
    }

    /**
     * Removes a tag.
     *
     * @param key the key
     */
    public ChunkCompoundTag removeChunkCompound(final ChunkUUID key) {
        ChunkCompoundTag compoundTag = this.chunks.remove(key);
        if (this.caching) {
            if (!this.canChangeToCache())
                this.changeToMemory();
        } else {
            if (this.canChangeToCache())
                this.changeToCache();
        }
        return compoundTag;
    }

    /**
     * Checks if this compound has a tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a tag with the specified key
     */
    public boolean containsChunkCompounds(final ChunkUUID key) {
        return this.chunks.containsKey(key);
    }

    public int sizeChunkCompounds() {
        return this.chunks.size();
    }

    public boolean isChunkCompoundsEmpty() {
        return this.chunks.isEmpty();
    }

    /**
     * Gets a set of keys of the entries in this compound tag.
     *
     * @return a set of keys
     */
    public Set<ChunkUUID> keySetChunkCompounds() {
        return this.chunks.keySet();
    }

    public Set<Map.Entry<ChunkUUID, ChunkCompoundTag>> entrySetChunkCompounds() {
        return this.chunks.entrySet();
    }

    public Collection<ChunkCompoundTag> chunkCompoundsValues() {
        return this.chunks.values();
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException(String.format("Depth of %d is higher than max of %d", depth, MAX_DEPTH));
        }
        int total = input.readInt(); //read total size
        if (this.caching) {
            if (total < 63)
                this.changeToMemory();
        } else {
            if (total >= 63)
                this.changeToCache();
        }
        while (input.readByte() == (byte) 1) {
            final int x = input.readInt();
            final int z = input.readInt();
            final ChunkUUID key = new ChunkUUID(x, z);
            final ChunkCompoundTag tag = new ChunkCompoundTag();
            tag.read(input, depth + 1);
            this.chunks.put(key, tag);
        }
        if (input.readByte() == (byte) 2) { //last byte
            this.regionTag.read(input, depth + 1);
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        int total = this.regionTag.size();
        for (ChunkCompoundTag tag : this.chunks.values())
            total += tag.sizeBlocks() + tag.sizeEntities();
        output.writeInt(total); // total size
        for (Map.Entry<ChunkUUID, ChunkCompoundTag> chunkCompoundEntry : this.chunks.entrySet()) {
            final ChunkCompoundTag tag = chunkCompoundEntry.getValue();
            if (tag.isBlocksEmpty() && tag.isEntitiesEmpty() && tag.getChunkCompound().isEmpty())
                continue; //skip some bytes
            final ChunkUUID key = chunkCompoundEntry.getKey();
            output.writeByte((byte) 1);
            output.writeInt(key.getX());
            output.writeInt(key.getZ());
            tag.write(output);
        }
        output.writeByte((byte) 0);
        if (!this.regionTag.isEmpty()) {
            output.writeByte((byte) 2); //write for chunk tag
            this.regionTag.write(output);
        }
        output.writeByte((byte) 0);
    }

    @Override
    public TagType type() {
        return TagType.REGION_COMPOUND;
    }

    @Override
    public RegionCompoundTag copy() {
        final RegionCompoundTag copy = new RegionCompoundTag();
        this.chunks.forEach((key, value) -> copy.putChunkCompound(key, value.copy()));
        copy.getRegionCompound().copyFrom(this.regionTag);
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chunks, this.regionTag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RegionCompoundTag))
            return false;
        RegionCompoundTag that = (RegionCompoundTag) obj;
        return this.chunks.equals(that.chunks) && this.regionTag.equals(that.regionTag);
    }

    @Override
    public void close() {
        if (!this.mapDB.isClosed())
            this.mapDB.close();
    }
}

