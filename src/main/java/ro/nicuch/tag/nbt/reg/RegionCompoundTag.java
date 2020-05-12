package ro.nicuch.tag.nbt.reg;

import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.Tag;
import ro.nicuch.tag.nbt.TagType;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A compound tag.
 */
public final class RegionCompoundTag implements Tag {
    /**
     * The maximum depth.
     */
    public static final int MAX_DEPTH = 512;
    /**
     * The map of tags.
     */
    private final ConcurrentMap<ChunkUUID, ChunkCompoundTag> chunks = new ConcurrentHashMap<>();

    private final CompoundTag regionTag = new CompoundTag();

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
        chunks.clear();
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
        return this.chunks.put(key, tag);
    }

    /**
     * Removes a tag.
     *
     * @param key the key
     */
    public ChunkCompoundTag removeChunkCompound(final ChunkUUID key) {
        return this.chunks.remove(key);
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
        byte type;
        while ((type = input.readByte()) != (byte) 0) {
            if (type == (byte) 1) {
                final int x = input.readInt();
                final int z = input.readInt();
                final ChunkUUID key = new ChunkUUID(x, z);
                final ChunkCompoundTag tag = new ChunkCompoundTag();
                tag.read(input, depth + 1);
                this.chunks.put(key, tag);
            } else if (type == (byte) 2) { //last byte
                this.regionTag.read(input, depth + 1);
            }
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
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
}

