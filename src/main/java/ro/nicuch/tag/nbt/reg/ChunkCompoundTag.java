package ro.nicuch.tag.nbt.reg;

import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.Tag;
import ro.nicuch.tag.nbt.TagType;
import ro.nicuch.tag.wrapper.BlockUUID;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
    private final ConcurrentMap<BlockUUID, CompoundTag> blocks = new ConcurrentHashMap<>();

    /**
     * The map of entities tags.
     */
    private final ConcurrentMap<UUID, CompoundTag> entities = new ConcurrentHashMap<>();

    private final CompoundTag chunktag = new CompoundTag();

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
     * Clear the entities tag.
     */
    public void clearEntities() {
        this.entities.clear();
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
     * Gets a entity tag by its key.
     *
     * @param key the key
     * @return the tag, or {@code null}
     */
    public CompoundTag getEntity(final UUID key) {
        return this.entities.get(key);
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
     * Inserts a entity tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public CompoundTag putEntity(final UUID key, final CompoundTag tag) {
        return this.entities.put(key, tag);
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
     * Removes a block tag.
     *
     * @param key the key
     */
    public CompoundTag removeEntity(final UUID key) {
        return this.entities.remove(key);
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

    /**
     * Checks if this compound has a entity tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a entity tag with the specified key
     */
    public boolean containsEntity(final UUID key) {
        return this.entities.containsKey(key);
    }

    public int sizeBlocks() {
        return this.blocks.size();
    }

    public boolean isBlocksEmpty() {
        return this.blocks.isEmpty();
    }

    public int sizeEntities() {
        return this.entities.size();
    }

    public boolean isEntitiesEmpty() {
        return this.entities.isEmpty();
    }

    /**
     * Gets a set of keys of the entries in this compound tag.
     *
     * @return a set of keys
     */
    public Set<BlockUUID> keySetBlocks() {
        return this.blocks.keySet();
    }

    /**
     * Gets a set of keys of the entries in this compound tag.
     *
     * @return a set of keys
     */
    public Set<UUID> keySetEntities() {
        return this.entities.keySet();
    }

    public Set<Map.Entry<BlockUUID, CompoundTag>> entrySetBlocks() {
        return this.blocks.entrySet();
    }

    public Collection<CompoundTag> blocksValues() {
        return this.blocks.values();
    }

    public Set<Map.Entry<UUID, CompoundTag>> entrySetEntities() {
        return this.entities.entrySet();
    }

    public Collection<CompoundTag> entitiesValues() {
        return this.entities.values();
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException(String.format("Depth of %d is higher than max of %d", depth, MAX_DEPTH));
        }
        byte type;
        while ((type = input.readByte()) == (byte) 1) {
            final int x = input.readInt();
            final int y = input.readInt();
            final int z = input.readInt();
            final BlockUUID key = new BlockUUID(x, y, z);
            final CompoundTag tag = new CompoundTag();
            tag.read(input, depth + 1);
            this.blocks.put(key, tag);
        }
        if (type == (byte) 2) { //don't skip a byte
            final String id = input.readUTF();
            final UUID key = UUID.fromString(id);
            final CompoundTag tag = new CompoundTag();
            tag.read(input, depth + 1);
            this.entities.put(key, tag);
        }
        while ((type = input.readByte()) == (byte) 2) {
            final String id = input.readUTF();
            final UUID key = UUID.fromString(id);
            final CompoundTag tag = new CompoundTag();
            tag.read(input, depth + 1);
            this.entities.put(key, tag);
        }
        if (type == (byte) 3) { //last byte
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
            output.writeInt(key.getX());
            output.writeInt(key.getY());
            output.writeInt(key.getZ());
            tag.write(output);
        }
        for (Map.Entry<UUID, CompoundTag> entitiesEntry : this.entities.entrySet()) {
            final UUID key = entitiesEntry.getKey();
            final CompoundTag tag = entitiesEntry.getValue();
            output.writeByte((byte) 2); //write for entities
            output.writeUTF(key.toString());
            tag.write(output);
        }
        if (!this.chunktag.isEmpty()) {
            output.writeByte((byte) 3); //write for chunk tag
            this.chunktag.write(output);
        }
        output.writeByte((byte) 0); // 0 means no more bytes to read
    }

    @Override
    public TagType type() {
        return TagType.CHUNK_COMPOUND;
    }

    @Override
    public ChunkCompoundTag copy() {
        final ChunkCompoundTag copy = new ChunkCompoundTag();
        this.blocks.forEach((key, value) -> copy.putBlock(key, value.copy()));
        this.entities.forEach((key, value) -> copy.putEntity(key, value.copy()));
        copy.getChunkCompound().copyFrom(this.chunktag);
        return copy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.blocks, this.entities, this.chunktag);
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj)
            return false;
        if (!(obj instanceof ChunkCompoundTag))
            return false;
        ChunkCompoundTag that = (ChunkCompoundTag) obj;
        return this.blocks.equals(that.blocks) && this.entities.equals(that.entities) && this.chunktag.equals(that.chunktag);
    }
}

