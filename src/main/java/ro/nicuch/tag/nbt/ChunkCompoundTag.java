package ro.nicuch.tag.nbt;

import ro.nicuch.tag.wrapper.BlockUUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * A compound tag.
 */
public final class ChunkCompoundTag implements Tag {
    /**
     * The maximum depth.
     */
    public static final int MAX_DEPTH = 512;
    /**
     * The map of blocks tags.
     */
    private final Map<BlockUUID, CompoundTag> blocks = new HashMap<>();

    private final CompoundTag chunktag = new CompoundTag();

    public boolean isEmpty(boolean removeEmpty) {
        if (removeEmpty) {
            this.blocks.values().removeIf(CompoundTag::isEmpty);
        }
        return this.blocks.isEmpty() && this.chunktag.isEmpty();
    }

    public CompoundTag getChunkCompound() {
        return this.chunktag;
    }

    /**
     * Clear the blocks tag.
     */
    public void clearBlocks() {
        this.blocks.clear();
    }

    /**
     * Gets a block tag by its key.
     *
     * @param key the key
     * @return the tag, or {@code null}
     */
    public CompoundTag getBlock(final BlockUUID key) {
        return this.blocks.get(key);
    }

    /**
     * Inserts a block tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public CompoundTag putBlock(final BlockUUID key, final CompoundTag tag) {
        return this.blocks.put(key, tag);
    }

    /**
     * Removes a block tag.
     *
     * @param key the key
     */
    public CompoundTag removeBlock(final BlockUUID key) {
        return this.blocks.remove(key);
    }

    /**
     * Checks if this compound has a block tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a block tag with the specified key
     */
    public boolean containsBlock(final BlockUUID key) {
        return this.blocks.containsKey(key);
    }

    public int sizeBlocks() {
        return this.blocks.size();
    }

    public boolean isBlocksEmpty() {
        return this.blocks.isEmpty();
    }

    /**
     * Gets a set of keys of the entries in this compound tag.
     *
     * @return a set of keys
     */
    public Set<BlockUUID> keySetBlocks() {
        return this.blocks.keySet();
    }

    public Set<Map.Entry<BlockUUID, CompoundTag>> entrySetBlocks() {
        return this.blocks.entrySet();
    }

    public Collection<CompoundTag> blocksValues() {
        return this.blocks.values();
    }


    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException(String.format("Depth of %d is higher than max of %d", depth, MAX_DEPTH));
        }
        while (input.readByte() == (byte) 1) {
            final byte x = input.readByte();
            final byte y = input.readByte();
            final byte z = input.readByte();
            final BlockUUID key = new BlockUUID(x, y, z);
            final CompoundTag tag = new CompoundTag();
            tag.read(input, depth + 1);
            this.blocks.put(key, tag);
        }
        if (input.readByte() == (byte) 2) { //last byte
            this.chunktag.read(input, depth + 1);
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        for (Map.Entry<BlockUUID, CompoundTag> blocksEntry : this.blocks.entrySet()) {
            final CompoundTag tag = blocksEntry.getValue();
            if (tag.isEmpty())
                continue; //skip some bytes
            final BlockUUID key = blocksEntry.getKey();
            output.writeByte((byte) 1); //write for blocks
            output.writeByte(key.getX());
            output.writeByte(key.getY());
            output.writeByte(key.getZ());
            tag.write(output);
        }
        output.writeByte((byte) 0); // 0 means end
        if (!this.chunktag.isEmpty()) {
            output.writeByte((byte) 2); //write for chunk tag
            this.chunktag.write(output);
        }
        output.writeByte((byte) 0); // 0 means end
    }

    @Override
    public TagType type() {
        return TagType.CHUNK_COMPOUND;
    }

    @Override
    public ChunkCompoundTag copy() {
        final ChunkCompoundTag copy = new ChunkCompoundTag();
        this.blocks.forEach((key, value) -> copy.putBlock(key, value.copy()));
        copy.getChunkCompound().copyFrom(this.chunktag);
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.blocks, this.chunktag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ChunkCompoundTag))
            return false;
        ChunkCompoundTag that = (ChunkCompoundTag) obj;
        return this.blocks.equals(that.blocks) && this.chunktag.equals(that.chunktag);
    }
}

