package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * A compound tag.
 */
public final class CompoundTag implements CollectionTag {

    public static CompoundTag from(ByteBuffer buffer, int length) {
        return new CompoundTag().read(buffer, length);
    }

    /**
     * The maximum depth.
     */
    public static final int MAX_DEPTH = 512;
    /**
     * The map of tags.
     */
    private final Map<String, Tag> tags = new HashMap<>();

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
    public Tag get(final String key) {
        return this.tags.get(key);
    }

    /**
     * Inserts a tag.
     *
     * @param key the key
     * @param tag the tag
     */
    public Tag put(final String key, final Tag tag) {
        return this.tags.put(key, tag);
    }

    /**
     * Removes a tag.
     *
     * @param key the key
     */
    public void remove(final String key) {
        this.tags.remove(key);
    }

    /**
     * Checks if this compound has a tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a tag with the specified key
     */
    public boolean contains(final String key) {
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
    public Set<String> keySet() {
        return this.tags.keySet();
    }

    public Set<Map.Entry<String, Tag>> entrySet() {
        return this.tags.entrySet();
    }

    public Collection<Tag> values() {
        return this.tags.values();
    }

    /**
     * Checks if this compound has a tag with the specified key and type.
     *
     * @param key  the key
     * @param type the type
     * @return {@code true} if this compound has a tag with the specified key and type
     */
    public boolean contains(final String key, final TagType type) {
        final Tag tag = this.tags.get(key);
        return tag != null && type.test(this.tags.get(key).type());
    }

    /**
     * Gets the tag type of the tag with the specified key.
     *
     * @param key the key
     * @return the tag type, or null
     */
    public TagType type(final String key) {
        final Tag tag = this.tags.get(key);
        return tag != null ? tag.type() : null;
    }

    /**
     * Gets a byte.
     *
     * @param key the key
     * @return the byte value, or {@code 0} if this compound does not contain a byte tag
     * with the specified key, or has a tag with a different type
     */
    public byte getByte(final String key) {
        return this.getByte(key, (byte) 0);
    }

    /**
     * Gets a byte.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the byte value, or {@code defaultValue} if this compound does not contain a byte tag
     * with the specified key, or has a tag with a different type
     */
    public byte getByte(final String key, final byte defaultValue) {
        if (this.contains(key, TagType.BYTE))
            return ((NumberTag) this.tags.get(key)).byteValue();
        return defaultValue;
    }

    /**
     * Inserts a byte.
     *
     * @param key   the key
     * @param value the value
     */
    public byte putByte(final String key, final byte value) {
        this.tags.put(key, new ByteTag(value));
        return value;
    }

    /**
     * Gets a short.
     *
     * @param key the key
     * @return the short value, or {@code 0} if this compound does not contain a short tag
     * with the specified key, or has a tag with a different type
     */
    public short getShort(final String key) {
        return this.getShort(key, (short) 0);
    }

    /**
     * Gets a short.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the short value, or {@code defaultValue} if this compound does not contain a short tag
     * with the specified key, or has a tag with a different type
     */
    public short getShort(final String key, final short defaultValue) {
        if (this.contains(key, TagType.SHORT))
            return ((NumberTag) this.tags.get(key)).shortValue();
        return defaultValue;
    }

    /**
     * Inserts a short.
     *
     * @param key   the key
     * @param value the value
     */
    public short putShort(final String key, final short value) {
        this.tags.put(key, new ShortTag(value));
        return value;
    }

    /**
     * Gets an int.
     *
     * @param key the key
     * @return the int value, or {@code 0} if this compound does not contain an int tag
     * with the specified key, or has a tag with a different type
     */
    public int getInt(final String key) {
        return this.getInt(key, 0);
    }

    /**
     * Gets an int.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the int value, or {@code defaultValue} if this compound does not contain an int tag
     * with the specified key, or has a tag with a different type
     */
    public int getInt(final String key, final int defaultValue) {
        if (this.contains(key, TagType.INT))
            return ((NumberTag) this.tags.get(key)).intValue();
        return defaultValue;
    }

    /**
     * Inserts an int.
     *
     * @param key   the key
     * @param value the value
     */
    public int putInt(final String key, final int value) {
        this.tags.put(key, new IntTag(value));
        return value;
    }

    /**
     * Gets a long.
     *
     * @param key the key
     * @return the long value, or {@code 0} if this compound does not contain a long tag
     * with the specified key, or has a tag with a different type
     */
    public long getLong(final String key) {
        return this.getLong(key, 0L);
    }

    /**
     * Gets a long.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the long value, or {@code defaultValue} if this compound does not contain a long tag
     * with the specified key, or has a tag with a different type
     */
    public long getLong(final String key, final long defaultValue) {
        if (this.contains(key, TagType.LONG))
            return ((NumberTag) this.tags.get(key)).longValue();
        return defaultValue;
    }

    /**
     * Inserts a long.
     *
     * @param key   the key
     * @param value the value
     */
    public long putLong(final String key, final long value) {
        this.tags.put(key, new LongTag(value));
        return value;
    }

    /**
     * Gets a float.
     *
     * @param key the key
     * @return the float value, or {@code 0} if this compound does not contain a float tag
     * with the specified key, or has a tag with a different type
     */
    public float getFloat(final String key) {
        return this.getFloat(key, 0f);
    }

    /**
     * Gets a float.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the float value, or {@code defaultValue} if this compound does not contain a float tag
     * with the specified key, or has a tag with a different type
     */
    public float getFloat(final String key, final float defaultValue) {
        if (this.contains(key, TagType.FLOAT))
            return ((NumberTag) this.tags.get(key)).floatValue();
        return defaultValue;
    }

    /**
     * Inserts a float.
     *
     * @param key   the key
     * @param value the value
     */
    public float putFloat(final String key, final float value) {
        this.tags.put(key, new FloatTag(value));
        return value;
    }

    /**
     * Gets a double.
     *
     * @param key the key
     * @return the double value, or {@code 0} if this compound does not contain a double tag
     * with the specified key, or has a tag with a different type
     */
    public double getDouble(final String key) {
        return this.getDouble(key, 0d);
    }

    /**
     * Gets a double.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the double value, or {@code defaultValue} if this compound does not contain a double tag
     * with the specified key, or has a tag with a different type
     */
    public double getDouble(final String key, final double defaultValue) {
        if (this.contains(key, TagType.DOUBLE))
            return ((NumberTag) this.tags.get(key)).doubleValue();
        return defaultValue;
    }

    /**
     * Inserts a double.
     *
     * @param key   the key
     * @param value the value
     */
    public double putDouble(final String key, final double value) {
        this.tags.put(key, new DoubleTag(value));
        return value;
    }

    /**
     * Gets an array of bytes.
     *
     * @param key the key
     * @return the array of bytes, or a zero-length array if this compound does not contain a byte array tag
     * with the specified key, or has a tag with a different type
     */
    public byte[] getByteArray(final String key) {
        if (this.contains(key, TagType.BYTE_ARRAY))
            return ((ByteArrayTag) this.tags.get(key)).value();
        return new byte[0];
    }

    /**
     * Gets an array of bytes.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the array of bytes, or {@code defaultValue}
     */
    public byte[] getByteArray(final String key, final byte[] defaultValue) {
        if (this.contains(key, TagType.BYTE_ARRAY))
            return ((ByteArrayTag) this.tags.get(key)).value();
        return defaultValue;
    }

    /**
     * Inserts an array of bytes.
     *
     * @param key   the key
     * @param value the value
     */
    public byte[] putByteArray(final String key, final byte[] value) {
        this.tags.put(key, new ByteArrayTag(value));
        return value;
    }

    /**
     * Gets a string.
     *
     * @param key the key
     * @return the string value, or {@code ""} if this compound does not contain a string tag
     * with the specified key, or has a tag with a different type
     */
    public String getString(final String key) {
        return this.getString(key, "");
    }

    /**
     * Gets a string.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the string value, or {@code defaultValue} if this compound does not contain a string tag
     * with the specified key, or has a tag with a different type
     */
    public String getString(final String key, final String defaultValue) {
        if (this.contains(key, TagType.STRING))
            return ((StringTag) this.tags.get(key)).value();
        return defaultValue;
    }

    /**
     * Inserts a string.
     *
     * @param key   the key
     * @param value the value
     */
    public String putString(final String key, final String value) {
        this.tags.put(key, new StringTag(value));
        return value;
    }

    /**
     * Gets a list.
     *
     * @param key the key
     * @return the list, or a new list if this compound does not contain a list tag
     * with the specified key, or has a tag with a different type
     */
    public ListTag getList(final String key) {
        if (this.contains(key, TagType.LIST))
            return (ListTag) this.tags.get(key);
        return new ListTag();
    }

    /**
     * Gets a list, ensuring that the type is the same as {@code type}.
     *
     * @param key          the key
     * @param expectedType the expected list type
     * @return the list, or a new list if this compound does not contain a list tag
     * with the specified key, has a tag with a different type, or the {@link ListTag#listType() list type}
     * does not match {@code expectedType}
     */
    public ListTag getList(final String key, final TagType expectedType) {
        if (this.contains(key, TagType.LIST)) {
            final ListTag tag = (ListTag) this.get(key);
            if (tag == null)
                return new ListTag();
            if (expectedType.test(tag.listType()))
                return tag;
        }
        return new ListTag();
    }

    /**
     * Gets a list, ensuring that the type is the same as {@code type}.
     *
     * @param key          the key
     * @param expectedType the expected list type
     * @param defaultValue the default value
     * @return the list, or {@code defaultValue} if this compound does not contain a list tag
     * with the specified key, has a tag with a different type, or the {@link ListTag#listType() list type}
     * does not match {@code expectedType}
     */
    public ListTag getList(final String key, final TagType expectedType, final ListTag defaultValue) {
        if (this.contains(key, TagType.LIST)) {
            final ListTag tag = (ListTag) this.get(key);
            if (tag == null)
                return defaultValue;
            if (expectedType.test(tag.listType()))
                return tag;
        }
        return defaultValue;
    }

    /**
     * Gets a list.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the list, or {@code defaultValue} if this compound does not contain a list tag
     * with the specified key, or has a tag with a different type
     */
    public ListTag getList(final String key, final ListTag defaultValue) {
        if (this.contains(key, TagType.LIST))
            return (ListTag) this.tags.get(key);
        return defaultValue;
    }

    /**
     * Gets a compound.
     *
     * @param key the key
     * @return the compound, or a new compound if this compound does not contain a compound tag
     * with the specified key, or has a tag with a different type
     */
    public CompoundTag getCompound(final String key) {
        if (this.contains(key, TagType.COMPOUND))
            return (CompoundTag) this.tags.get(key);
        return new CompoundTag();
    }

    /**
     * Gets a compound.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the compound, or {@code defaultValue} if this compound does not contain a compound tag
     * with the specified key, or has a tag with a different type
     */
    public CompoundTag getCompound(final String key, final CompoundTag defaultValue) {
        if (this.contains(key, TagType.COMPOUND))
            return (CompoundTag) this.tags.get(key);
        return defaultValue;
    }

    /**
     * Inserts a compound.
     *
     * @param key   the key
     * @param value the value
     */
    public CompoundTag putCompound(final String key, final CompoundTag value) {
        this.tags.put(key, value);
        return value;
    }

    /**
     * Gets a compound.
     *
     * @param key the key
     * @return the compound, or a new compound if this compound does not contain a compound tag
     * with the specified key, or has a tag with a different type
     */
    public ChunkCompoundTag getChunkCompound(final String key) {
        if (this.contains(key, TagType.CHUNK))
            return (ChunkCompoundTag) this.tags.get(key);
        return new ChunkCompoundTag();
    }

    /**
     * Gets an array of ints.
     *
     * @param key the key
     * @return the array of ints, or a zero-length array if this compound does not contain a int array tag
     * with the specified key, or has a tag with a different type
     */
    public int[] getIntArray(final String key) {
        if (this.contains(key, TagType.INT_ARRAY))
            return ((IntArrayTag) this.tags.get(key)).value();
        return new int[0];
    }

    /**
     * Gets an array of ints.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the array of ints, or {@code defaultValue}
     */
    public int[] getIntArray(final String key, final int[] defaultValue) {
        if (this.contains(key, TagType.INT_ARRAY))
            return ((IntArrayTag) this.tags.get(key)).value();
        return defaultValue;
    }

    /**
     * Inserts an array of ints.
     *
     * @param key   the key
     * @param value the value
     */
    public int[] putIntArray(final String key, final int[] value) {
        this.tags.put(key, new IntArrayTag(value));
        return value;
    }

    /**
     * Gets an array of longs.
     *
     * @param key the key
     * @return the array of longs, or a zero-length array if this compound does not contain a long array tag
     * with the specified key, or has a tag with a different type
     */
    public long[] getLongArray(final String key) {
        if (this.contains(key, TagType.BYTE_ARRAY))
            return ((LongArrayTag) this.tags.get(key)).value();
        return new long[0];
    }

    /**
     * Gets an array of longs.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the array of longs, or {@code defaultValue}
     */
    public long[] getLongArray(final String key, final long[] defaultValue) {
        if (this.contains(key, TagType.BYTE_ARRAY))
            return ((LongArrayTag) this.tags.get(key)).value();
        return defaultValue;
    }

    /**
     * Inserts an array of longs.
     *
     * @param key   the key
     * @param value the value
     */
    public long[] putLongArray(final String key, final long[] value) {
        this.tags.put(key, new LongArrayTag(value));
        return value;
    }

    /**
     * Gets an array of strings.
     *
     * @param key the key
     * @return the array of strings, or a zero-length array if this compound does not contain a string array tag
     * with the specified key, or has a tag with a different type
     */
    public String[] getStringArray(final String key) {
        if (this.contains(key, TagType.BYTE_ARRAY))
            return ((StringArrayTag) this.tags.get(key)).value();
        return new String[0];
    }

    /**
     * Gets an array of strings.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the array of strings, or {@code defaultValue}
     */
    public String[] getStringArray(final String key, final String[] defaultValue) {
        if (this.contains(key, TagType.BYTE_ARRAY))
            return ((StringArrayTag) this.tags.get(key)).value();
        return defaultValue;
    }

    /**
     * Inserts an array of strings.
     *
     * @param key   the key
     * @param value the value
     */
    public String[] putStringArray(final String key, final String[] value) {
        this.tags.put(key, new StringArrayTag(value));
        return value;
    }

    /**
     * Gets a boolean.
     *
     * <p>A boolean is stored as a {@code byte} internally.</p>
     *
     * @param key the key
     * @return the boolean, or {@code false} if this compound does not contain a boolean with
     * the specified key, or has a tag with a different type
     */
    public boolean getBoolean(final String key) {
        return this.getBoolean(key, false);
    }

    /**
     * Gets a boolean.
     *
     * <p>A boolean is stored as a {@code byte} internally.</p>
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the boolean, or {@code defaultValue} if this compound does not contain a boolean with
     * the specified key, or has a tag with a different type
     */
    public boolean getBoolean(final String key, final boolean defaultValue) {
        // >=, as this can be something other than a byte
        return this.getByte(key, defaultValue ? ByteTag.TRUE : ByteTag.FALSE) >= ByteTag.TRUE;
    }

    /**
     * Inserts a boolean.
     *
     * <p>A boolean is stored as a {@code byte} internally.</p>
     *
     * @param key   the key
     * @param value the value
     */
    public boolean putBoolean(final String key, final boolean value) {
        this.putByte(key, value ? ByteTag.TRUE : ByteTag.FALSE);
        return value;
    }

    /**
     * Checks if this compound has a boolean tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a boolean tag with the specified key
     */
    public boolean containsBoolean(final String key) {
        return this.contains(key, TagType.BYTE);
    }

    /**
     * Checks if this compound has a string tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a string tag with the specified key
     */
    public boolean containsString(final String key) {
        return this.contains(key, TagType.STRING);
    }

    /**
     * Checks if this compound has a string array tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a string array tag with the specified key
     */
    public boolean containsStringArray(final String key) {
        return this.contains(key, TagType.STRING_ARRAY);
    }

    /**
     * Checks if this compound has a int tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a int tag with the specified key
     */
    public boolean containsInt(final String key) {
        return this.contains(key, TagType.INT);
    }

    /**
     * Checks if this compound has a int array tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a int array tag with the specified key
     */
    public boolean containsIntArray(final String key) {
        return this.contains(key, TagType.INT_ARRAY);
    }

    /**
     * Checks if this compound has a big int tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a big int tag with the specified key
     */
    public boolean containsBigInt(final String key) {
        return this.contains(key, TagType.BIG_INT);
    }

    /**
     * Checks if this compound has a byte tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a byte tag with the specified key
     */
    public boolean containsByte(final String key) {
        return this.contains(key, TagType.BYTE);
    }

    /**
     * Checks if this compound has a byte array tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a byte array tag with the specified key
     */
    public boolean containsByteArray(final String key) {
        return this.contains(key, TagType.BYTE_ARRAY);
    }

    /**
     * Checks if this compound has a compound tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a compound tag with the specified key
     */
    public boolean containsCompound(final String key) {
        return this.contains(key, TagType.COMPOUND);
    }

    /**
     * Checks if this compound has a compound tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a compound tag with the specified key
     */
    public boolean containsChunkCompound(final String key) {
        return this.contains(key, TagType.CHUNK);
    }

    /**
     * Checks if this compound has a double tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a double tag with the specified key
     */
    public boolean containsDouble(final String key) {
        return this.contains(key, TagType.DOUBLE);
    }

    /**
     * Checks if this compound has a float tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a float tag with the specified key
     */
    public boolean containsFloat(final String key) {
        return this.contains(key, TagType.FLOAT);
    }

    /**
     * Checks if this compound has a list tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a list tag with the specified key
     */
    public boolean containsList(final String key) {
        return this.contains(key, TagType.LIST);
    }

    /**
     * Checks if this compound has a long tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a long tag with the specified key
     */
    public boolean containsLong(final String key) {
        return this.contains(key, TagType.LONG);
    }

    /**
     * Checks if this compound has a long array tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a long array tag with the specified key
     */
    public boolean containsLongArray(final String key) {
        return this.contains(key, TagType.LONG_ARRAY);
    }

    /**
     * Checks if this compound has a short tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a short tag with the specified key
     */
    public boolean containsShort(final String key) {
        return this.contains(key, TagType.SHORT);
    }

    /**
     * Gets a unique id.
     *
     * <p>A unique id is stored as two {@code long}s internally.</p>
     *
     * @param key the key
     * @return the unique id
     */
    public UUID getUniqueId(final String key) {
        return UUID.fromString(this.getString(key + "UUIDHash"));
    }

    /**
     * Inserts a unique id.
     *
     * <p>A unique id is stored as two {@code long}s internally.</p>
     *
     * @param key   the key
     * @param value the value
     */
    public UUID putUniqueId(final String key, final UUID value) {
        this.putString(key + "UUIDHash", value.toString());
        return value;
    }

    /**
     * Checks if this compound has a unique id tag with the specified key.
     *
     * @param key the key
     * @return {@code true} if this compound has a unique id tag with the specified key
     */
    public boolean containsUniqueId(final String key) {
        return this.contains(key + "UUIDHash", TagType.STRING);
    }

    @Override
    public int bufferDataSize() {
        return 4 // 4 bytes for size
                + this.tags.keySet().stream().mapToInt(str -> str.length() * 2).sum() // x bytes for each key, 2 bytes for each char
                + this.tags.values().stream().mapToInt(tag -> tag.bufferDataSize() + tag.type().bufferDataSize()).sum(); // x bytes for each tag + type
    }

    @Override
    public CompoundTag read(ByteBuffer input, int depth) {
        if (depth > MAX_DEPTH)
            throw new IllegalStateException(String.format("Depth of %d is higher than max of %d", depth, MAX_DEPTH));
        int size = input.getInt();
        for (int n = 0; n < size; n++) {
            TagType type = TagType.of(input);
            final int keyLength = input.getInt();
            final char[] keyArr = new char[keyLength];
            for (int i = 0; i < keyLength; i++)
                keyArr[i] = input.getChar();
            final String key = new String(keyArr);
            final Tag tag = type.create();
            tag.read(input, depth + 1);
            this.tags.put(key, tag);
        }
        return this;
    }

    @Override
    public CompoundTag write(ByteBuffer output) {
        output.putInt(this.tags.size());
        for (Map.Entry<String, Tag> tagsEntry : this.tags.entrySet()) {
            final Tag tag = tagsEntry.getValue();
            if (tag instanceof CollectionTag)
                if (((CollectionTag) tag).isEmpty())
                    continue; //skip empty collection tags
            tag.type().to(output);
            final String key = tagsEntry.getKey();
            final char[] keyArr = key.toCharArray();
            output.putInt(keyArr.length);
            for (char keyCh : keyArr)
                output.putChar(keyCh);
            tag.write(output);
        }
        return this;
    }

    @Override
    public TagType type() {
        return TagType.COMPOUND;
    }

    @Override
    public CompoundTag copy() {
        final CompoundTag copy = new CompoundTag();
        this.tags.forEach((key, value) -> copy.put(key, value.copy()));
        return copy;
    }

    public void forEach(BiConsumer<? super String, ? super Tag> action) {
        for (Map.Entry<String, Tag> entry : this.tags.entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    public void copyFrom(CompoundTag from) {
        from.forEach((key, value) -> this.tags.put(key, value.copy()));
    }

    @Override
    public int hashCode() {
        return this.tags.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof CompoundTag && this.tags.equals(((CompoundTag) that).tags));
    }
}
