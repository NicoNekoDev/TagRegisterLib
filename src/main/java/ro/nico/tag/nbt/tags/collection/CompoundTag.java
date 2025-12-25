package ro.nico.tag.nbt.tags.collection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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


/**
 * The compound tag (type ID 10) is used for storing an unordered map of any and all named tags.
 * All tags present in a compound must be given a name (key). Every valid NBT data structure is contained entirely within a "root" compound.
 *
 * @author dewy
 */
@AllArgsConstructor
public class CompoundTag extends Tag implements SnbtSerializable, JsonSerializable, Iterable<Tag> {
    private @NotNull Map<String, Tag> value;

    /**
     * Constructs an empty, unnamed compound tag.
     */
    public CompoundTag() {
        this(null, new LinkedHashMap<>());
    }

    /**
     * Constructs an empty compound tag with a given name.
     *
     * @param name the tag's name.
     */
    public CompoundTag(String name) {
        this(name, new LinkedHashMap<>());
    }

    /**
     * Constructs a compound tag with a given name and {@code Map<>} value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code Map<>} value.
     */
    public CompoundTag(String name, @NotNull Map<String, Tag> value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.COMPOUND;
    }

    @Override
    public @NotNull Map<String, Tag> getValue() {
        return this.value;
    }

    /**
     * Sets the {@code Map<>} value of this compound tag.
     *
     * @param value new {@code Map<>} value to be set.
     */
    public void setValue(@NotNull Map<String, Tag> value) {
        this.value = value;
    }

    @Override
    public CompoundTag write(DataOutput output, int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        for (Tag tag : this) {
            output.writeByte(tag.getType().getId());
            output.writeUTF(tag.getName());

            tag.write(output, depth + 1);
        }

        output.writeByte(0);

        return this;
    }

    @Override
    public CompoundTag read(DataInput input, int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        Map<String, Tag> tags = new LinkedHashMap<>();

        byte nextTypeId;
        Tag nextTag;
        while ((nextTypeId = input.readByte()) != 0) {
            Supplier<Tag> tagFactory = TagTypeRegistry.getFactoryFromId(nextTypeId);

            if (tagFactory == null) {
                throw new IOException("Tag type with ID " + nextTypeId + " not present in tag type registry.");
            }

            nextTag = tagFactory.get();

            nextTag.setName(input.readUTF());
            nextTag.read(input, depth + 1);

            tags.put(nextTag.getName(), nextTag);
        }

        this.value = tags;

        return this;
    }

    @Override
    public JsonObject toJson(int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        JsonObject json = new JsonObject();
        JsonObject value = new JsonObject();
        json.addProperty("type", this.getType().getName());

        if (this.getName() != null) {
            json.addProperty("name", this.getName());
        }

        for (Tag tag : this) {
            try {
                value.add(tag.getName(), ((JsonSerializable) tag).toJson(depth + 1));
            } catch (ClassCastException e) {
                throw new IOException("Tag not JsonSerializable.", e);
            }
        }

        json.add("value", value);

        return json;
    }

    @Override
    public CompoundTag fromJson(JsonObject json, int depth) throws IOException {
        if (depth > 512) {
            throw new IOException("NBT structure too complex (depth > 512).");
        }

        this.clear();

        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        Map<String, Tag> tags = new LinkedHashMap<>();

        String nextTypeId;
        Tag nextTag;
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("value").entrySet()) {
            JsonObject entryJson = entry.getValue().getAsJsonObject();

            nextTypeId = entryJson.getAsJsonPrimitive("type").getAsString();
            Supplier<Tag> tagFactory = TagTypeRegistry.getFactoryFromId(nextTypeId);

            if (tagFactory == null) {
                throw new IOException("Tag type with name " + nextTypeId + " not present in tag type registry.");
            }

            nextTag = tagFactory.get();

            ((JsonSerializable) nextTag).fromJson(entryJson, depth + 1);
            tags.put(nextTag.getName(), nextTag);
        }

        this.value = tags;

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        if (this.value.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");

        if (config.isPrettyPrint()) {
            sb.append('\n').append(StringUtils.multiplyIndent(depth + 1, config));
        }

        boolean first = true;
        for (Tag tag : this) {
            if (!first) {
                if (config.isPrettyPrint()) {
                    sb.append(",\n").append(StringUtils.multiplyIndent(depth + 1, config));
                } else {
                    sb.append(',');
                }
            }

            sb.append(StringUtils.escapeSnbt(tag.getName()));

            if (config.isPrettyPrint()) {
                sb.append(": ");
            } else {
                sb.append(':');
            }

            sb.append(((SnbtSerializable) tag).toSnbt(depth + 1, config));

            if (first) {
                first = false;
            }
        }

        if (config.isPrettyPrint()) {
            sb.append("\n").append(StringUtils.multiplyIndent(depth, config)).append('}');
        } else {
            sb.append('}');
        }

        return sb.toString();
    }

    /**
     * Returns the number of entries in this compound tag.
     *
     * @return the number of entries in this compound tag.
     */
    public int size() {
        return this.value.size();
    }

    /**
     * Returns true if this compound tag is empty, false otherwise.
     *
     * @return true if this compound tag is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    /**
     * Adds a given tag to this compound. The tag must have a name, or NPE is thrown.
     *
     * @param tag the named tag to be added to the compound.
     * @return the previous value mapped with the tag's name as type E if provided, or null if there wasn't any.
     * @throws NullPointerException if the tag's name is null.
     */
    public Tag put(@NotNull Tag tag) {
        return this.value.put(tag.getName(), tag);
    }

    /**
     * Adds a given tag to this compound. Be careful, the tag's name is set to the {@code name} parameter automatically.
     *
     * @param name the tag's name (key).
     * @param tag  the tag to be added to the compound.
     * @return the previous value mapped with the tag's name as type E if provided, or null if there wasn't any.
     */
    public Tag put(@NotNull String name, @NotNull Tag tag) {
        tag.setName(name);

        return this.put(tag);
    }

    public void putByte(@NotNull String name, byte value) {
        this.put(name, new ByteTag(name, value));
    }

    public void putShort(@NotNull String name, short value) {
        this.put(name, new ShortTag(name, value));
    }

    public void putInt(@NotNull String name, int value) {
        this.put(name, new IntTag(name, value));
    }

    public void putLong(@NotNull String name, long value) {
        this.put(name, new LongTag(name, value));
    }

    public void putFloat(@NotNull String name, float value) {
        this.put(name, new FloatTag(name, value));
    }

    public void putDouble(@NotNull String name, double value) {
        this.put(name, new DoubleTag(name, value));
    }

    public void putByteArray(@NotNull String name, byte @NotNull [] value) {
        this.put(name, new ByteArrayTag(name, value));
    }

    public void putString(@NotNull String name, @NotNull String value) {
        this.put(name, new StringTag(name, value));
    }

    public <C extends Tag> void putList(@NotNull String name, List<C> value) {
        this.put(name, new ListTag<>(name, value));
    }

    public void putCompound(@NotNull String name, @NotNull Map<String, Tag> value) {
        this.put(name, new CompoundTag(name, value));
    }

    public void putIntArray(@NotNull String name, int @NotNull [] value) {
        this.put(name, new IntArrayTag(name, value));
    }

    public void putLongArray(@NotNull String name, long @NotNull [] value) {
        this.put(name, new LongArrayTag(name, value));
    }

    /**
     * Removes a tag from this compound with a given name (key).
     *
     * @param key the name whose mapping is to be removed from this compound.
     * @return the previous value associated with {@code key} as type T if provided.
     */
    public Tag remove(@NotNull String key) {
        return this.value.remove(key);
    }

    /**
     * Retrieves a tag from this compound with a given name (key).
     *
     * @param key the name whose mapping is to be retrieved from this compound.
     * @return the value associated with {@code key} as type T.
     */
    public @Nullable Tag get(@NotNull String key) {
        return this.value.get(key);
    }

    public Tag get(@NotNull String key, Tag defaultValue, TagType tagType) {
        Tag tag = this.value.getOrDefault(key, defaultValue);

        if (tag.getType() != tagType) return defaultValue;

        return tag;
    }

    public @Nullable Tag get(@NotNull String key, TagType tagType) {
        return this.get(key, null, tagType);
    }

    public @Nullable ByteTag getByteTag(@NotNull String key) {
        return (ByteTag) this.get(key, TagType.STRING);
    }

    public ByteTag getByteTag(@NotNull String key, ByteTag defaultValue) {
        return (ByteTag) this.get(key, defaultValue, TagType.STRING);
    }

    public @Nullable ShortTag getShortTag(@NotNull String key) {
        return (ShortTag) this.get(key, TagType.SHORT);
    }

    public ShortTag getShortTag(@NotNull String key, ShortTag defaultValue) {
        return (ShortTag) this.get(key, defaultValue, TagType.SHORT);
    }

    public @Nullable IntTag getIntTag(@NotNull String key) {
        return (IntTag) this.get(key, TagType.INT);
    }

    public IntTag getIntTag(@NotNull String key, IntTag defaultValue) {
        return (IntTag) this.get(key, defaultValue, TagType.INT);
    }

    public @Nullable LongTag getLongTag(@NotNull String key) {
        return (LongTag) this.get(key, TagType.LONG);
    }

    public LongTag getLongTag(@NotNull String key, LongTag defaultValue) {
        return (LongTag) this.get(key, defaultValue, TagType.LONG);
    }

    public @Nullable FloatTag getFloatTag(@NotNull String key) {
        return (FloatTag) this.get(key, TagType.FLOAT);
    }

    public FloatTag getFloatTag(@NotNull String key, FloatTag defaultValue) {
        return (FloatTag) this.get(key, defaultValue, TagType.FLOAT);
    }

    public @Nullable DoubleTag getDoubleTag(@NotNull String key) {
        return (DoubleTag) this.get(key, TagType.DOUBLE);
    }

    public DoubleTag getDoubleTag(@NotNull String key, DoubleTag defaultValue) {
        return (DoubleTag) this.get(key, defaultValue, TagType.DOUBLE);
    }

    public @Nullable ByteArrayTag getByteArrayTag(@NotNull String key) {
        return (ByteArrayTag) this.get(key, TagType.BYTE_ARRAY);
    }

    public ByteArrayTag getByteArrayTag(@NotNull String key, ByteArrayTag defaultValue) {
        return (ByteArrayTag) this.get(key, defaultValue, TagType.BYTE_ARRAY);
    }

    public @Nullable StringTag getStringTag(@NotNull String key) {
        return (StringTag) this.get(key, TagType.STRING);
    }

    public StringTag getStringTag(@NotNull String key, StringTag defaultValue) {
        return (StringTag) this.get(key, defaultValue, TagType.STRING);
    }

    @SuppressWarnings("unchecked")
    public <C extends Tag> @Nullable ListTag<C> getListTag(@NotNull String key) {
        return (ListTag<C>) this.get(key, TagType.LIST);
    }

    @SuppressWarnings("unchecked")
    public <C extends Tag> ListTag<C> getListTag(@NotNull String key, ListTag<C> defaultValue) {
        return (ListTag<C>) this.get(key, defaultValue, TagType.LIST);
    }

    public @Nullable CompoundTag getCompoundTag(@NotNull String key) {
        return (CompoundTag) this.get(key, TagType.COMPOUND);
    }

    public CompoundTag getCompoundTag(@NotNull String key, CompoundTag defaultValue) {
        return (CompoundTag) this.get(key, defaultValue, TagType.COMPOUND);
    }

    public @Nullable IntArrayTag getIntArrayTag(@NotNull String key) {
        return (IntArrayTag) this.get(key, TagType.INT_ARRAY);
    }

    public IntArrayTag getIntArrayTag(@NotNull String key, IntArrayTag defaultValue) {
        return (IntArrayTag) this.get(key, defaultValue, TagType.INT_ARRAY);
    }

    public @Nullable LongArrayTag getLongArrayTag(@NotNull String key) {
        return (LongArrayTag) this.get(key, TagType.LONG_ARRAY);
    }

    public LongArrayTag getLongArrayTag(@NotNull String key, LongArrayTag defaultValue) {
        return (LongArrayTag) this.get(key, defaultValue, TagType.LONG_ARRAY);
    }

    /**
     * Returns true if this compound contains an entry with a given name (key), false otherwise.
     *
     * @param key the name (key) to check for.
     * @return true if this compound contains an entry with a given name (key), false otherwise.
     */
    public boolean contains(@NotNull String key) {
        return this.value.containsKey(key);
    }

    /**
     * Returns true if this compound contains an entry with a given name (key) and if that entry is of a given tag type, false otherwise.
     *
     * @param key  the name (key) to check for.
     * @param type the tag type to test for.
     * @return true if this compound contains an entry with a given name (key) and if that entry is of a given tag type, false otherwise.
     */
    public boolean contains(@NotNull String key, TagType type) {
        Tag tag = this.get(key);
        if (tag == null)
            return true;

        return tag.getType() == type;
    }

    public boolean containsByte(@NotNull String key) {
        return this.contains(key, TagType.BYTE);
    }

    public boolean containsShort(@NotNull String key) {
        return this.contains(key, TagType.SHORT);
    }

    public boolean containsInt(@NotNull String key) {
        return this.contains(key, TagType.INT);
    }

    public boolean containsLong(@NotNull String key) {
        return this.contains(key, TagType.LONG);
    }

    public boolean containsFloat(@NotNull String key) {
        return this.contains(key, TagType.FLOAT);
    }

    public boolean containsDouble(@NotNull String key) {
        return this.contains(key, TagType.DOUBLE);
    }

    public boolean containsByteArray(@NotNull String key) {
        return this.contains(key, TagType.BYTE_ARRAY);
    }

    public boolean containsString(@NotNull String key) {
        return this.contains(key, TagType.STRING);
    }

    public boolean containsList(@NotNull String key) {
        return this.contains(key, TagType.LIST);
    }

    public boolean containsListOf(@NotNull String key, TagType of) {
        ListTag<?> list = this.getListTag(key);

        if (list == null)
            return true;

        return list.getListType() == of;
    }

    public boolean containsCompound(@NotNull String key) {
        return this.contains(key, TagType.COMPOUND);
    }

    public boolean containsIntArray(@NotNull String key) {
        return this.contains(key, TagType.INT_ARRAY);
    }

    public boolean containsLongArray(@NotNull String key) {
        return this.contains(key, TagType.LONG_ARRAY);
    }

    /**
     * Returns all {@link Tag}s contained within this compound.
     *
     * @return all {@link Tag}s contained within this compound.
     */
    public Collection<Tag> values() {
        return this.value.values();
    }

    /**
     * Returns a {@code Set<>} of all names (keys) currently used within this compound.
     *
     * @return a {@code Set<>} of all names (keys) currently used within this compound.
     */
    public Set<String> keySet() {
        return this.value.keySet();
    }

    /**
     * Removes all entries from the compound. The compound will be empty after this call returns.
     */
    public void clear() {
        this.value.clear();
    }

    @Override
    public @NotNull Iterator<Tag> iterator() {
        return this.value.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super Tag> action) {
        this.value.values().forEach(action);
    }

    @Override
    public Spliterator<Tag> spliterator() {
        return this.value.values().spliterator();
    }

    @Override
    public String toString() {
        return this.toSnbt(0, new SnbtConfig());
    }

    @Override
    public CompoundTag copy() {
        CompoundTag copy = new CompoundTag();
        this.value.forEach((key, value) -> copy.put(key, value.copy()));
        return copy;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof CompoundTag other && this.value.equals(other.value));
    }
}
