package ro.nicuch.tag.nbt;

import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A compound tag.
 */
public final class ChunkCompoundTag implements CollectionTag {
    /**
     * The maximum depth.
     */
    public static final int MAX_DEPTH = 512;
    /**
     * The map of tags.
     */
    private final ConcurrentMap<ChunkUUID, CompoundTag> tags = new ConcurrentHashMap<>();

    /**
     * Clear the tag.
     */
    public void clear() {
        tags.clear();
    }

    /**
     * Gets a tag by its key.
     *
     * @param key the key
     * @return the tag, or {@code null}
     */
    public CompoundTag get(final ChunkUUID key) {
        return this.tags.get(key);
    }

    /**
     * Inserts a tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public CompoundTag put(final ChunkUUID key, final CompoundTag tag) {
        return this.tags.put(key, tag);
    }

    /**
     * Removes a tag.
     *
     * @param key the key
     */
    public void remove(final ChunkUUID key) {
        this.tags.remove(key);
    }

    /**
     * Checks if this compound has a tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a tag with the specified key
     */
    public boolean contains(final ChunkUUID key) {
        return this.tags.containsKey(key);
    }

    @Override
    public int size() {
        return this.tags.size();
    }

    @Override
    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    /**
     * Gets a set of keys of the entries in this compound tag.
     *
     * @return a set of keys
     */
    public Set<ChunkUUID> keySet() {
        return this.tags.keySet();
    }

    public Set<Map.Entry<ChunkUUID, CompoundTag>> entrySet() {
        return this.tags.entrySet();
    }

    public Collection<CompoundTag> values() {
        return this.tags.values();
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException(String.format("Depth of %d is higher than max of %d", depth, MAX_DEPTH));
        }
        while (input.readByte() == (byte) 1) {
            final int x = input.readInt();
            final int z = input.readInt();
            final ChunkUUID key = new ChunkUUID(x, z);
            final CompoundTag tag = new CompoundTag();
            tag.read(input, depth + 1);
            this.tags.put(key, tag);
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        for (final ChunkUUID key : this.tags.keySet()) {
            CompoundTag tag = this.tags.get(key);
            output.writeByte((byte) 1);
            output.writeInt(key.getX());
            output.writeInt(key.getZ());
            tag.write(output);
        }
        output.writeByte((byte) 0);
    }

    @Override
    public TagType type() {
        return TagType.CHUNK_COMPOUND;
    }

    @Override
    public ChunkCompoundTag copy() {
        final ChunkCompoundTag copy = new ChunkCompoundTag();
        this.tags.forEach((key, value) -> copy.put(key, value.copy()));
        return copy;
    }

    @Override
    public int hashCode() {
        return this.tags.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ChunkCompoundTag && this.tags.equals(((ChunkCompoundTag) that).tags));
    }
}
