package ro.nico.tag.nbt.tags.collection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ro.nico.tag.nbt.api.Tag;
import ro.nico.tag.nbt.api.json.JsonSerializable;
import ro.nico.tag.nbt.api.registry.TagTypeRegistry;
import ro.nico.tag.nbt.api.snbt.SnbtConfig;
import ro.nico.tag.nbt.api.snbt.SnbtSerializable;
import ro.nico.tag.nbt.tags.TagType;
import ro.nico.tag.nbt.tags.array.ByteArrayTag;
import ro.nico.tag.nbt.tags.array.IntArrayTag;
import ro.nico.tag.nbt.tags.array.LongArrayTag;
import ro.nico.tag.nbt.tags.primitive.*;
import ro.nico.tag.nbt.utils.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * The list tag (type ID 9) is used for storing an ordered list of unnamed NBT tags all of the same type.
 *
 * @author dewy
 */
@AllArgsConstructor
public class ListTag<T extends Tag> extends Tag implements SnbtSerializable, JsonSerializable, Iterable<T> {
    private @NotNull List<T> value;
    private TagType type;

    /**
     * Constructs an empty, unnamed list tag.
     */
    public ListTag() {
        this(null);
    }

    /**
     * Constructs an empty list tag with a given name.
     *
     * @param name the tag's name.
     */
    public ListTag(String name) {
        this(name, new LinkedList<>());
    }

    /**
     * Constructs a list tag with a given name and {@code List<>} value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code List<>} value.
     */
    public ListTag(String name, @NotNull List<T> value) {
        if (value.isEmpty()) {
            this.type = TagType.END;
        } else {
            this.type = value.getFirst().getType();
        }

        this.setName(name);
        this.setValue(value);
    }

    /**
     * Creates a list tag with some double values.
     *
     * @param values the double values
     * @return the list tag
     */
    public static @NotNull ListTag<ByteTag> bytes(byte @NotNull ... values) {
        final ListTag<ByteTag> tag = new ListTag<>();

        for (int value : values)
            tag.add(new ByteTag(value));

        return tag;
    }

    /**
     * Creates a list tag with some double values.
     *
     * @param values the double values
     * @return the list tag
     */
    public static @NotNull ListTag<IntTag> integers(int @NotNull ... values) {
        ListTag<IntTag> tag = new ListTag<>();

        for (int value : values)
            tag.add(new IntTag(value));

        return tag;
    }

    /**
     * Creates a list tag with some double values.
     *
     * @param values the double values
     * @return the list tag
     */
    public static @NotNull ListTag<DoubleTag> doubles(double @NotNull ... values) {
        final ListTag<DoubleTag> tag = new ListTag<>();

        for (final double value : values)
            tag.add(new DoubleTag(value));

        return tag;
    }

    /**
     * Creates a list tag with some double values.
     *
     * @param values the double values
     * @return the list tag
     */
    public static @NotNull ListTag<LongTag> longs(long @NotNull ... values) {
        ListTag<LongTag> tag = new ListTag<>();

        for (long value : values)
            tag.add(new LongTag(value));

        return tag;
    }

    /**
     * Creates a list tag with some float values.
     *
     * @param values the float values
     * @return the list tag
     */
    public static @NotNull ListTag<FloatTag> floats(float @NotNull ... values) {
        ListTag<FloatTag> tag = new ListTag<>();

        for (float value : values)
            tag.add(new FloatTag(value));

        return tag;
    }

    /**
     * Creates a list tag with some string values.
     *
     * @param values the string values
     * @return the list tag
     */
    public static @NotNull ListTag<StringTag> strings(@NotNull String... values) {
        ListTag<StringTag> tag = new ListTag<>();

        for (int i = 0, length = values.length; i < length; i++)
            tag.add(new StringTag(requireNonNull(values[i], "value at index " + i)));

        return tag;
    }

    @Override
    public TagType getType() {
        return TagType.LIST;
    }

    @Override
    public @NotNull List<T> getValue() {
        return this.value;
    }

    /**
     * Returns the ID of the NBT tag type this list holds.
     *
     * @return the ID of the NBT tag type this list holds.
     */
    public TagType getListType() {
        return this.type;
    }

    /**
     * Sets the {@code List<>} value of this list tag.
     *
     * @param value new {@code List<>} value to be set.
     */
    public void setValue(@NotNull List<T> value) {
        if (value.isEmpty()) {
            this.type = TagType.END;
        } else {
            this.type = value.getFirst().getType();
        }

        this.value = value;
    }

    @Override
    public ListTag<T> write(DataOutput output, int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        output.writeByte(this.type.getId());
        output.writeInt(this.value.size());

        for (Tag tag : this) {
            tag.write(output, depth + 1);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ListTag<T> read(DataInput input, int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        List<T> tags = new ArrayList<>();

        byte tagType = input.readByte();
        int length = input.readInt();

        Tag next;
        for (int i = 0; i < length; i++) {
            Supplier<Tag> tagFactory = TagTypeRegistry.getFactoryFromId(tagType);

            if (tagFactory == null) {
                throw new IOException("Tag type with ID " + tagType + " not present in tag type registry.");
            }

            next = tagFactory.get();

            next.read(input, depth + 1);
            next.setName(null);

            tags.add((T) next);
        }

        if (tags.isEmpty()) {
            this.type = TagType.END;
        } else {
            this.type = tags.getFirst().getType();
        }

        this.value = tags;

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        StringBuilder sb = new StringBuilder("[");

        if (config.isPrettyPrint()) {
            sb.append('\n').append(StringUtils.multiplyIndent(depth + 1, config));
        }

        for (int i = 0; i < this.value.size(); ++i) {
            if (i != 0) {
                if (config.isPrettyPrint()) {
                    sb.append(",\n").append(StringUtils.multiplyIndent(depth + 1, config));
                } else {
                    sb.append(',');
                }
            }

            sb.append(((SnbtSerializable) this.value.get(i)).toSnbt(depth + 1, config));
        }

        if (config.isPrettyPrint()) {
            sb.append("\n").append(StringUtils.multiplyIndent(depth, config)).append(']');
        } else {
            sb.append(']');
        }

        return sb.toString();
    }

    @Override
    public JsonObject toJson(int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        JsonObject json = new JsonObject();
        JsonArray value = new JsonArray();

        json.addProperty("type", this.getType().getName());
        json.addProperty("listType", this.getListType().getName());

        if (this.getName() != null) {
            json.addProperty("name", this.getName());
        }

        for (Tag tag : this) {
            tag.setName(null);
            value.add(((JsonSerializable) tag).toJson(depth + 1));
        }

        json.add("value", value);

        return json;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ListTag<T> fromJson(JsonObject json, int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        this.clear();

        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        String listType = json.getAsJsonPrimitive("listType").getAsString();
        List<T> tags = new LinkedList<>();

        Tag nextTag;
        for (JsonElement element : json.getAsJsonArray("value")) {
            Supplier<Tag> tagFactory = TagTypeRegistry.getFactoryFromId(listType);

            if (tagFactory == null) {
                throw new IOException("Tag type with name " + listType + " not present in tag type registry.");
            }

            nextTag = tagFactory.get();

            ((JsonSerializable) nextTag).fromJson((JsonObject) element, depth + 1);
            tags.add((T) nextTag);
        }

        if (tags.isEmpty()) {
            this.type = TagType.END;
        } else {
            this.type = tags.getFirst().getType();
        }

        this.value = tags;

        return this;
    }

    /**
     * Gets a byte.
     *
     * @param index the index
     * @return the byte value, or {@code 0}
     */
    public byte getByte(int index) {
        return this.getByte(index, (byte) 0);
    }

    /**
     * Gets a byte.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the byte value, or {@code defaultValue}
     */
    public byte getByte(int index, byte defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType().isNumber())
            return ((NumericalTag<?>) tag).byteValue();

        return defaultValue;
    }

    public ByteTag getByteTag(int index) {
        return this.getByteTag(index, null);
    }

    public ByteTag getByteTag(int index, ByteTag defaultValue) {
        return (ByteTag) this.get(index, defaultValue, TagType.BYTE);
    }

    /**
     * Gets a short.
     *
     * @param index the index
     * @return the short value, or {@code 0}
     */
    public short getShort(int index) {
        return this.getShort(index, (short) 0);
    }

    /**
     * Gets a short.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the short value, or {@code defaultValue}
     */
    public short getShort(int index, short defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType().isNumber())
            return ((NumericalTag<?>) tag).shortValue();

        return defaultValue;
    }

    public ShortTag getShortTag(int index) {
        return this.getShortTag(index, null);
    }

    public ShortTag getShortTag(int index, ShortTag defaultValue) {
        return (ShortTag) this.get(index, defaultValue, TagType.SHORT);
    }

    /**
     * Gets an int.
     *
     * @param index the index
     * @return the int value, or {@code 0}
     */
    public int getInt(int index) {
        return this.getInt(index, 0);
    }

    /**
     * Gets an int.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the int value, or {@code defaultValue}
     */
    public int getInt(int index, int defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType().isNumber())
            return ((NumericalTag<?>) tag).intValue();

        return defaultValue;
    }

    public IntTag getIntTag(int index) {
        return this.getIntTag(index, null);
    }

    public IntTag getIntTag(int index, IntTag defaultValue) {
        return (IntTag) this.get(index, defaultValue, TagType.INT);
    }

    /**
     * Gets a long.
     *
     * @param index the index
     * @return the long value, or {@code 0}
     */
    public long getLong(int index) {
        return this.getLong(index, 0L);
    }

    /**
     * Gets a long.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the long value, or {@code defaultValue}
     */
    public long getLong(int index, long defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType().isNumber())
            return ((NumericalTag<?>) tag).longValue();

        return defaultValue;
    }

    public LongTag getLongTag(int index) {
        return this.getLongTag(index, null);
    }

    public LongTag getLongTag(int index, LongTag defaultValue) {
        return (LongTag) this.get(index, defaultValue, TagType.LONG);
    }

    /**
     * Gets a float.
     *
     * @param index the index
     * @return the float value, or {@code 0}
     */
    public float getFloat(int index) {
        return this.getFloat(index, 0f);
    }

    /**
     * Gets a float.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the float value, or {@code defaultValue}
     */
    public float getFloat(int index, float defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType().isNumber())
            return ((NumericalTag<?>) tag).floatValue();

        return defaultValue;
    }

    public FloatTag getFloatTag(int index) {
        return this.getFloatTag(index, null);
    }

    public FloatTag getFloatTag(int index, FloatTag defaultValue) {
        return (FloatTag) this.get(index, defaultValue, TagType.FLOAT);
    }

    /**
     * Gets a double.
     *
     * @param index the index
     * @return the double value, or {@code 0}
     */
    public double getDouble(int index) {
        return this.getDouble(index, 0d);
    }

    /**
     * Gets a double.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the double value, or {@code defaultValue}
     */
    public double getDouble(int index, double defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType().isNumber())
            return ((NumericalTag<?>) tag).doubleValue();

        return defaultValue;
    }

    public DoubleTag getDoubleTag(int index) {
        return this.getDoubleTag(index, null);
    }

    public DoubleTag getDoubleTag(int index, DoubleTag defaultValue) {
        return (DoubleTag) this.get(index, defaultValue, TagType.FLOAT);
    }

    /**
     * Gets an array of bytes.
     *
     * @param index the index
     * @return the array of bytes, or a zero-length array
     */
    public byte[] getByteArray(int index) {
        return this.getByteArray(index, new byte[0]);
    }

    /**
     * Gets an array of bytes.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the array of bytes, or {@code defaultValue}
     */
    public byte[] getByteArray(int index, byte[] defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType() == TagType.BYTE_ARRAY)
            return ((ByteArrayTag) tag).getValue();

        return defaultValue;
    }

    public ByteArrayTag getByteArrayTag(int index) {
        return this.getByteArrayTag(index, null);
    }

    public ByteArrayTag getByteArrayTag(int index, ByteArrayTag defaultValue) {
        return (ByteArrayTag) this.get(index, defaultValue, TagType.BYTE_ARRAY);
    }

    /**
     * Gets a string.
     *
     * @param index the index
     * @return the string value, or {@code ""}
     */
    public String getString(int index) {
        return this.getString(index, "");
    }

    /**
     * Gets a string.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the string value, or {@code defaultValue}
     */
    public String getString(int index, String defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType() == TagType.STRING)
            return ((StringTag) tag).getValue();

        return defaultValue;
    }

    public StringTag getStringTag(int index) {
        return this.getStringTag(index, null);
    }

    public StringTag getStringTag(int index, StringTag defaultValue) {
        return (StringTag) this.get(index, defaultValue, TagType.STRING);
    }

    /**
     * Gets a compound.
     *
     * @param index the index
     * @return the compound, or null
     */
    public CompoundTag getCompoundTag(int index) {
        return this.getCompoundTag(index, null);
    }

    /**
     * Gets a compound.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the compound, or {@code defaultValue}
     */
    public CompoundTag getCompoundTag(int index, CompoundTag defaultValue) {
        return (CompoundTag) this.get(index, defaultValue, TagType.COMPOUND);
    }

    /**
     * Gets a list.
     *
     * @param index the index
     * @return the list, or null
     */
    public <C extends Tag> ListTag<C> getListTag(int index) {
        return this.getListTag(index, null);
    }

    /**
     * Gets a list.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the list, or {@code defaultValue}
     */
    @SuppressWarnings("unchecked")
    public <C extends Tag> ListTag<C> getListTag(int index, ListTag<C> defaultValue) {
        return (ListTag<C>) this.get(index, defaultValue, TagType.LIST);
    }

    /**
     * Gets an array of ints.
     *
     * @param index the index
     * @return the array of ints, or a zero-length array
     */
    public int[] getIntArray(int index) {
        return this.getIntArray(index, new int[0]);
    }

    /**
     * Gets an array of ints.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the array of ints, or {@code defaultValue}
     */
    public int[] getIntArray(int index, int[] defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType() == TagType.INT_ARRAY)
            return ((IntArrayTag) tag).getValue();

        return defaultValue;
    }

    public IntArrayTag getIntArrayTag(int index) {
        return this.getIntArrayTag(index, null);
    }

    public IntArrayTag getIntArrayTag(int index, IntArrayTag defaultValue) {
        return (IntArrayTag) this.get(index, defaultValue, TagType.INT_ARRAY);
    }

    /**
     * Gets an array of longs.
     *
     * @param index the index
     * @return the array of longs, or a zero-length array
     */
    public long[] getLongArray(int index) {
        return this.getLongArray(index, new long[0]);
    }

    /**
     * Gets an array of longs.
     *
     * @param index        the index
     * @param defaultValue the default value
     * @return the array of longs, or {@code defaultValue}
     */
    public long[] getLongArray(int index, long[] defaultValue) {
        Tag tag = this.get(index);

        if (tag.getType() == TagType.LONG_ARRAY)
            return ((LongArrayTag) tag).getValue();

        return defaultValue;
    }

    public LongArrayTag getLongArrayTag(int index) {
        return this.getLongArrayTag(index, null);
    }

    public LongArrayTag getLongArrayTag(int index, LongArrayTag defaultValue) {
        return (LongArrayTag) this.get(index, defaultValue, TagType.LONG_ARRAY);
    }

    /**
     * Retrieves a tag from its index in the list.
     *
     * @param index the index of the tag to be retrieved.
     * @return the tag at the specified index, or null if not found.
     */
    public Tag get(int index) {
        return this.get(index, (Tag) null);
    }

    /**
     * Retrieves a tag from its index in the list.
     *
     * @param index the index of the tag to be retrieved.
     * @return the tag at the specified index, or the default value if the index is out of bounds or null.
     */
    public Tag get(int index, Tag defaultValue) {
        if (index < 0 || index >= size())
            return defaultValue;

        Tag tag = this.value.get(index);

        if (tag == null)
            return defaultValue;

        return tag;
    }

    /**
     * Retrieves a tag from its index in the list.
     *
     * @param index the index of the tag to be retrieved.
     * @return the tag at the specified index, or null if not found.
     */
    public Tag get(int index, TagType tagType) {
        return this.get(index, null, tagType);
    }

    /**
     * Retrieves a tag from its index in the list.
     *
     * @param index the index of the tag to be retrieved.
     * @return the tag at the specified index, or the default value if the index is out of bounds or null.
     */
    public Tag get(int index, Tag defaultValue, TagType tagType) {
        if (index < 0 || index >= size())
            return defaultValue;

        Tag tag = this.value.get(index);

        if (tag == null || tag.getType() != tagType)
            return defaultValue;

        return tag;
    }

    /**
     * Returns the number of elements in this list tag.
     *
     * @return the number of elements in this list tag.
     */
    public int size() {
        return this.value.size();
    }

    /**
     * Returns true if this list tag is empty, false otherwise.
     *
     * @return true if this list tag is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    /**
     * Appends the specified tag to the end of the list. Returns true if added successfully.
     *
     * @param tag the tag to be added.
     * @return true if added successfully.
     */
    public boolean add(@NotNull T tag) {
        if (this.value.isEmpty()) {
            this.type = tag.getType();
        }

        if (tag.getType() != this.type) {
            return false;
        }

        return this.value.add(tag);
    }

    /**
     * Inserts the specified tag at the specified position in this list.
     * Shifts the tag currently at that position and any subsequent tags to the right.
     *
     * @param index index at which the tag is to be inserted.
     * @param tag   tag to be inserted.
     */
    public void insert(int index, @NotNull T tag) {
        if (this.value.isEmpty()) {
            this.type = tag.getType();
        }

        if (tag.getType() != this.type) {
            return;
        }

        this.value.add(index, tag);
    }

    /**
     * Removes a given tag from the list. Returns true if removed successfully, false otherwise.
     *
     * @param tag the tag to be removed.
     * @return true if the tag was removed successfully, false otherwise.
     */
    public boolean remove(@NotNull T tag) {
        boolean success = this.value.remove(tag);

        if (this.value.isEmpty()) {
            this.type = TagType.END;
        }

        return success;
    }

    /**
     * Removes a tag from the list based on the tag's index. Returns the removed tag.
     *
     * @param index the index of the tag to be removed.
     * @return the removed tag.
     */
    public Tag remove(int index) {
        Tag previous = this.value.remove(index);

        if (this.value.isEmpty()) {
            this.type = TagType.END;
        }

        return previous;
    }

    /**
     * Returns true if this list contains the tag, false otherwise.
     *
     * @param tag the tag to check for.
     * @return true if this list contains the tag, false otherwise.
     */
    public boolean contains(@NotNull T tag) {
        return this.value.contains(tag);
    }

    /**
     * Returns true if this list contains all tags in the collection, false otherwise.
     *
     * @param tags the tags to be checked for.
     * @return true if this list contains all tags in the collection, false otherwise.
     */
    @SuppressWarnings("SlowListContainsAll")
    public boolean containsAll(@NotNull Collection<T> tags) {
        return this.value.containsAll(tags);
    }

    /**
     * Removes all tags from the list. The list will be empty after this call returns.
     */
    public void clear() {
        this.type = TagType.END;
        this.value.clear();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.value.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        this.value.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return this.value.spliterator();
    }

    @Override
    public String toString() {
        return this.toSnbt(0, new SnbtConfig());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ListTag<T> copy() {
        return new ListTag<>(getName(), (List<T>) this.value.stream().map(T::copy).toList());
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ListTag<?> other && this.value.equals(other.value));
    }
}
