package ro.nicuch.tag.nbt;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An enumeration of tag types.
 */
public enum TagType implements Predicate<TagType> {
    /**
     * @see EndTag
     */
    END((byte) 0, EndTag::new),
    /**
     * @see ByteTag
     */
    BYTE((byte) 1, true, ByteTag::new),
    /**
     * @see ShortTag
     */
    SHORT((byte) 2, true, ShortTag::new),
    /**
     * @see IntTag
     */
    INT((byte) 3, true, IntTag::new),
    /**
     * @see LongTag
     */
    LONG((byte) 4, true, LongTag::new),
    /**
     * @see FloatTag
     */
    FLOAT((byte) 5, true, FloatTag::new),
    /**
     * @see DoubleTag
     */
    DOUBLE((byte) 6, true, DoubleTag::new),
    /**
     * @see ByteArrayTag
     */
    BYTE_ARRAY((byte) 7, ByteArrayTag::new),
    /**
     * @see StringTag
     */
    STRING((byte) 8, StringTag::new),
    /**
     * @see ListTag
     */
    LIST((byte) 9, ListTag::new),
    /**
     * @see CompoundTag
     */
    COMPOUND((byte) 10, CompoundTag::new),
    /**
     * @see IntArrayTag
     */
    INT_ARRAY((byte) 11, IntArrayTag::new),
    /**
     * @see LongArrayTag
     */
    LONG_ARRAY((byte) 12, LongArrayTag::new),
    /**
     * @see StringArrayTag
     */
    STRING_ARRAY((byte) 13, StringArrayTag::new),
    /**
     * @see BigIntegerTag
     */
    BIG_INT((byte) 14, BigIntegerTag::new),

    BLOCK_COMPOUND((byte) 15, BlockCompoundTag::new),

    CHUNK_COMPOUND((byte) 16, ChunkCompoundTag::new);

    private static final TagType[] TYPES = values();
    /**
     * The byte id of this tag type.
     */
    private final byte id;
    /**
     * If this tag type is a {@link NumberTag number} type.
     */
    private final boolean number;
    /**
     * The tag factory.
     */
    private final Supplier<Tag> factory;

    TagType(final byte id, final Supplier<Tag> factory) {
        this(id, false, factory);
    }

    TagType(final byte id, final boolean number, final Supplier<Tag> factory) {
        this.id = id;
        this.number = number;
        this.factory = factory;
    }

    /**
     * Gets the byte id of this tag type.
     *
     * @return the byte id
     */
    public byte id() {
        return this.id;
    }

    /**
     * Checks if this tag type is a {@link NumberTag number} type.
     *
     * @return {@code true} if a number type, {@code false} if not
     */
    public boolean number() {
        return this.number;
    }

    /**
     * Creates a new tag.
     *
     * @return a new tag
     */
    Tag create() {
        return this.factory.get();
    }

    @Override
    public boolean test(final TagType that) {
        return this == that || (this.number && that.number);
    }

    /**
     * Gets the tag type for the specified id.
     *
     * @param id the id
     * @return the tag type
     * @throws ArrayIndexOutOfBoundsException if the id is not without bounds
     */
    static TagType of(final byte id) {
        return TYPES[id];
    }
}
