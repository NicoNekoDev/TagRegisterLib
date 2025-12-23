package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An enumeration of tag types.
 */
public class TagType implements Predicate<TagType> {
    /**
     * @see EndTag
     */
    public static TagType END = new TagType("tag:end", EndTag::new);
    /**
     * @see ByteTag
     */
    public static TagType BYTE = new TagType("tag:byte", true, ByteTag::new);
    /**
     * @see ShortTag
     */
    public static TagType SHORT = new TagType("tag:short", true, ShortTag::new);
    /**
     * @see IntTag
     */
    public static TagType INT = new TagType("tag:int", true, IntTag::new);
    /**
     * @see LongTag
     */
    public static TagType LONG = new TagType("tag:long", true, LongTag::new);
    /**
     * @see FloatTag
     */
    public static TagType FLOAT = new TagType("tag:float", true, FloatTag::new);
    /**
     * @see DoubleTag
     */
    public static TagType DOUBLE = new TagType("tag:double", true, DoubleTag::new);
    /**
     * @see ByteArrayTag
     */
    public static TagType BYTE_ARRAY = new TagType("tag:byte-array", ByteArrayTag::new);
    /**
     * @see StringTag
     */
    public static TagType STRING = new TagType("tag:string", StringTag::new);
    /**
     * @see ListTag
     */
    public static TagType LIST = new TagType("tag:list", ListTag::new);
    /**
     * @see CompoundTag
     */
    public static TagType COMPOUND = new TagType("tag:compound", CompoundTag::new);
    /**
     * @see IntArrayTag
     */
    public static TagType INT_ARRAY = new TagType("tag:int-array", IntArrayTag::new);
    /**
     * @see LongArrayTag
     */
    public static TagType LONG_ARRAY = new TagType("tag:long-array", LongArrayTag::new);
    /**
     * @see StringArrayTag
     */
    public static TagType STRING_ARRAY = new TagType("tag:string-array", StringArrayTag::new);
    /**
     * @see BigIntegerTag
     */
    public static TagType BIG_INT = new TagType("tag:big-int", BigIntegerTag::new);
    /**
     * @see ChunkCompoundTag
     */
    public static TagType CHUNK = new TagType("tag:chunk", ChunkCompoundTag::new);

    private static final ConcurrentMap<String, TagType> REGISTRY = new ConcurrentHashMap<>();

    public static TagType register(final String id, final Supplier<Tag> factory) {
        return register(id, false, factory);
    }

    public static TagType register(final String id, final boolean number, final Supplier<Tag> factory) {
        if (REGISTRY.containsKey(id.toLowerCase()))
            throw new IllegalArgumentException("Tag type " + id + " is already registered!");
        return new TagType(id, number, factory);
    }

    /**
     * The byte id of this tag type.
     */
    private final String id;
    /**
     * If this tag type is a {@link NumberTag number} type.
     */
    private final boolean number;
    /**
     * The tag factory.
     */
    private final Supplier<Tag> factory;

    private TagType(final String id, final Supplier<Tag> factory) {
        this(id, false, factory);
    }

    private TagType(final String id, final boolean number, final Supplier<Tag> factory) {
        this.id = id;
        this.number = number;
        this.factory = factory;
        REGISTRY.put(id, this);
    }

    /**
     * Gets the byte id of this tag type.
     *
     * @return the byte id
     */
    public String id() {
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
    public Tag create() {
        return this.factory.get();
    }

    @Override
    public boolean test(final TagType that) {
        return this == that || (this.number && that.number);
    }

    public void to(final ByteBuffer buffer) {
        byte[] array = this.id.toLowerCase().getBytes(StandardCharsets.UTF_8);
        buffer.put((byte) array.length);
        buffer.put(array);
    }

    public int bufferDataSize() {
        return this.id.toLowerCase().getBytes(StandardCharsets.UTF_8).length + 1;
    }

    /**
     * Gets the tag type for the specified id.
     *
     * @param buffer the buffer
     * @return the tag type
     */
    public static TagType of(final ByteBuffer buffer) {
        byte[] array = new byte[buffer.get()];
        buffer.get(array);
        String type = new String(array, StandardCharsets.UTF_8);
        return REGISTRY.get(type.toLowerCase());
    }
}
