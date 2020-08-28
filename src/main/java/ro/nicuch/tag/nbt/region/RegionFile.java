package ro.nicuch.tag.nbt.region;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import ro.nicuch.tag.TagRegisterSerializer;
import ro.nicuch.tag.nbt.ChunkCompoundTag;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.Closeable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A compound tag.
 */
public final class RegionFile implements Closeable {
    /**
     * The maximum depth.
     */
    public static final int MAX_DEPTH = 512;
    /**
     * The map of tags.
     */
    private final ConcurrentMap<ChunkUUID, ChunkCompoundTag> chunks;
    private final ReentrantLock lock = new ReentrantLock();
    private final DB mapDB;

    private final CompoundTag regionTag = new CompoundTag();

    public RegionFile(final File regionFile, final int x, final int z) {
        this.mapDB = DBMaker.fileDB(regionFile).closeOnJvmShutdown().transactionEnable().make();
        this.chunks = this.mapDB.hashMap("region-" + x + "." + z).keySerializer(TagRegisterSerializer.CHUNK_SERIALIZER).valueSerializer(TagRegisterSerializer.CHUNK_COMPOUND_TAG_SERIALIZER).createOrOpen();
    }


    public boolean isEmpty(boolean removeEmpty) {
        this.lock.lock();
        try {
            if (removeEmpty)
                this.chunks.values().removeIf(compound -> compound.isEmpty(false));
            return this.chunks.isEmpty() && this.regionTag.isEmpty();
        } finally {
            this.lock.unlock();
        }
    }

    public CompoundTag getRegionCompound() {
        this.lock.lock();
        try {
            return this.regionTag;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Clear the tag.
     */
    public void clearChunkCompounds() {
        this.lock.lock();
        try {
            this.chunks.clear();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Gets a tag by its key.
     *
     * @param key the key
     * @return the tag, or {@code null}
     */
    public ChunkCompoundTag getChunkCompound(final ChunkUUID key) {
        this.lock.lock();
        try {
            return this.chunks.get(key);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Inserts a tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public ChunkCompoundTag putChunkCompound(final ChunkUUID key, final ChunkCompoundTag tag) {
        this.lock.lock();
        try {
            return this.chunks.put(key, tag);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Removes a tag.
     *
     * @param key the key
     */
    public ChunkCompoundTag removeChunkCompound(final ChunkUUID key) {
        this.lock.lock();
        try {
            return this.chunks.remove(key);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Checks if this compound has a tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a tag with the specified key
     */
    public boolean containsChunkCompounds(final ChunkUUID key) {
        this.lock.lock();
        try {
            return this.chunks.containsKey(key);
        } finally {
            this.lock.unlock();
        }
    }

    public int sizeChunkCompounds() {
        this.lock.lock();
        try {
            return this.chunks.size();
        } finally {
            this.lock.unlock();
        }
    }

    public boolean isChunkCompoundsEmpty() {
        this.lock.lock();
        try {
            return this.chunks.isEmpty();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Gets a set of keys of the entries in this compound tag.
     *
     * @return a set of keys
     */
    public Set<ChunkUUID> keySetChunkCompounds() {
        this.lock.lock();
        try {
            return this.chunks.keySet();
        } finally {
            this.lock.unlock();
        }
    }

    public Set<Map.Entry<ChunkUUID, ChunkCompoundTag>> entrySetChunkCompounds() {
        this.lock.lock();
        try {
            return this.chunks.entrySet();
        } finally {
            this.lock.unlock();
        }
    }

    public List<ChunkCompoundTag> chunkCompoundsValues() {
        this.lock.lock();
        try {
            return new ArrayList<>(this.chunks.values());
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chunks, this.regionTag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RegionFile))
            return false;
        RegionFile that = (RegionFile) obj;
        return this.chunks.equals(that.chunks) && this.regionTag.equals(that.regionTag);
    }

    @Override
    public void close() {
        if (!this.mapDB.isClosed())
            this.mapDB.close();
    }

    public void commit() {
        if (!this.mapDB.isClosed())
            this.mapDB.commit();
    }
}

