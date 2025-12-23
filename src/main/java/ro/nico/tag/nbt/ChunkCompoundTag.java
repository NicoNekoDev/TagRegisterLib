package ro.nico.tag.nbt;

import ro.nico.tag.wrapper.BlockPos;

import java.nio.ByteBuffer;
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
    private final Map<BlockPos, CompoundTag> blocks = new HashMap<>();

    private final CompoundTag chunkTag = new CompoundTag();

    public boolean isEmpty(boolean removeEmpty) {
        if (removeEmpty) {
            this.blocks.values().removeIf(CompoundTag::isEmpty);
        }
        return this.blocks.isEmpty() && this.chunkTag.isEmpty();
    }

    public CompoundTag getChunkCompound() {
        return this.chunkTag;
    }

    /**
     * Clear the blocks tag.
     *
     * @return itself
     */
    public ChunkCompoundTag clearBlocks() {
        this.blocks.clear();
        return this;
    }

    /**
     * Gets a block tag by its key.
     *
     * @param key the key
     * @return the tag, or {@code null}
     */
    public CompoundTag getBlock(final BlockPos key) {
        return this.blocks.get(key);
    }

    /**
     * Inserts a block tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public CompoundTag putBlock(final BlockPos key, final CompoundTag tag) {
        return this.blocks.put(key, tag);
    }

    /**
     * Removes a block tag.
     *
     * @param key the key
     */
    public CompoundTag removeBlock(final BlockPos key) {
        return this.blocks.remove(key);
    }

    /**
     * Checks if this compound has a block tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a block tag with the specified key
     */
    public boolean containsBlock(final BlockPos key) {
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
    public Set<BlockPos> keySetBlocks() {
        return this.blocks.keySet();
    }

    public Set<Map.Entry<BlockPos, CompoundTag>> entrySetBlocks() {
        return this.blocks.entrySet();
    }

    public Collection<CompoundTag> blocksValues() {
        return this.blocks.values();
    }


    @Override
    public int bufferDataSize() {
        return 4 // bytes for size of blocks
                + this.blocks.keySet().stream().mapToInt(BlockPos::bufferDataSize).sum() // x bytes per block pos
                + this.blocks.values().stream().mapToInt(Tag::bufferDataSize).sum() // x bytes per compound tag
                + this.chunkTag.bufferDataSize(); // x bytes for chunk tag
    }

    @Override
    public ChunkCompoundTag read(ByteBuffer input, int depth) {
        if (depth > MAX_DEPTH)
            throw new IllegalStateException(String.format("Depth of %d is higher than max of %d", depth, MAX_DEPTH));
        int size = input.getInt();
        for (int i = 0; i < size; i++)
            this.blocks.put(BlockPos.from(input), CompoundTag.from(input, depth + 1));
        this.chunkTag.read(input, depth + 1); //last bytes
        return this;
    }

    @Override
    public ChunkCompoundTag write(ByteBuffer output) {
        output.putInt(this.blocks.size()); // 4 bytes for size
        for (Map.Entry<BlockPos, CompoundTag> blocksEntry : this.blocks.entrySet()) {
            final CompoundTag tag = blocksEntry.getValue();
            if (tag.isEmpty())
                continue; //skip empty tags
            blocksEntry.getKey().to(output); // 3 bytes for each x,y,z coords
            tag.write(output); // x bytes for each
        }
        this.chunkTag.write(output); // x bytes for tag
        return this;
    }

    @Override
    public TagType type() {
        return TagType.CHUNK;
    }

    @Override
    public ChunkCompoundTag copy() {
        final ChunkCompoundTag copy = new ChunkCompoundTag();
        this.blocks.forEach((key, value) -> copy.putBlock(key, value.copy()));
        copy.getChunkCompound().copyFrom(this.chunkTag);
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.blocks, this.chunkTag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ChunkCompoundTag that))
            return false;
        return this.blocks.equals(that.blocks) && this.chunkTag.equals(that.chunkTag);
    }
}

