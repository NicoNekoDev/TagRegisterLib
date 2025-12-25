package ro.nico.tag.nbt.tags.array;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import ro.nico.tag.nbt.api.snbt.SnbtConfig;
import ro.nico.tag.nbt.tags.TagType;
import ro.nico.tag.nbt.tags.primitive.IntTag;
import ro.nico.tag.nbt.utils.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * The int array tag (type ID 11) is used for storing {@code int[]} arrays in NBT structures.
 * It is not stored as a list of {@link IntTag}s.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class IntArrayTag extends ArrayTag<Integer> {
    private @NotNull int[] value;

    /**
     * Constructs an int array tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code int[]} value.
     */
    public IntArrayTag(String name, @NotNull int[] value) {
        this.setName(name);
        this.setValue(value);
    }

    /**
     * Constructs an unnamed int array tag using a {@code List<>} object.
     *
     * @param value the tag's {@code List<>} value, to be converted to a primitive {@code int[]} array.
     */
    public IntArrayTag(@NotNull List<Integer> value) {
        this(null, value);
    }

    /**
     * Constructs an int array tag with a given name, using a List object to determine its {@code int[]} value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code List<>} value, to be converted to a primitive {@code int[]} array.
     */
    public IntArrayTag(String name, @NotNull List<Integer> value) {
        this.setName(name);
        this.setValue(ArrayUtils.toPrimitive(value.toArray(new Integer[0])));
    }

    @Override
    public TagType getType() {
        return TagType.INT_ARRAY;
    }

    @Override
    public int[] getValue() {
        return this.value;
    }

    /**
     * Sets the {@code int[]} value of this int array tag.
     *
     * @param value new {@code int[]} value to be set.
     */
    public void setValue(@NotNull int[] value) {
        this.value = value;
    }

    @Override
    public IntArrayTag write(DataOutput output, int depth) throws IOException {
        output.writeInt(this.value.length);

        for (int i : this) {
            output.writeInt(i);
        }

        return this;
    }

    @Override
    public IntArrayTag read(DataInput input, int depth) throws IOException {
        this.value = new int[input.readInt()];

        for (int i = 0; i < this.value.length; i++) {
            this.value[i] = input.readInt();
        }

        return this;
    }

    @Override
    public JsonObject toJson(int depth) throws IOException {
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        json.addProperty("type", this.getType().getName());

        if (this.getName() != null) {
            json.addProperty("name", this.getName());
        }

        for (int i : this) {
            array.add(i);
        }

        json.add("value", array);

        return json;
    }

    @Override
    public IntArrayTag fromJson(JsonObject json, int depth) throws IOException {
        JsonArray array = json.getAsJsonArray("value");

        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = new int[array.size()];

        for (int i = 0; i < array.size(); i++) {
            this.value[i] = array.get(i).getAsInt();
        }

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        StringBuilder sb = new StringBuilder("[I;");

        if (config.isPrettyPrint()) {
            if (this.value.length < config.getInlineThreshold()) {
                sb.append('\n').append(StringUtils.multiplyIndent(depth + 1, config));
            } else {
                sb.append(' ');
            }
        }

        for (int i = 0; i < this.value.length; ++i) {
            if (i != 0) {
                if (config.isPrettyPrint()) {
                    if (this.value.length < config.getInlineThreshold()) {
                        sb.append(",\n").append(StringUtils.multiplyIndent(depth + 1, config));
                    } else {
                        sb.append(", ");
                    }
                } else {
                    sb.append(',');
                }
            }

            sb.append(this.value[i]);
        }

        if (config.isPrettyPrint() && this.value.length < config.getInlineThreshold()) {
            sb.append("\n").append(StringUtils.multiplyIndent(depth, config)).append(']');
        } else {
            sb.append(']');
        }

        return sb.toString();
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public Integer get(int index) {
        return this.value[index];
    }

    @Override
    public Integer set(int index, @NotNull Integer element) {
        return this.value[index] = element;
    }

    @Override
    public void insert(int index, @NotNull Integer... elements) {
        this.value = ArrayUtils.insert(index, this.value, ArrayUtils.toPrimitive(elements));
    }

    @Override
    public Integer remove(int index) {
        Integer previous = this.value[index];
        this.value = ArrayUtils.remove(this.value, index);

        return previous;
    }

    @Override
    public void clear() {
        this.value = new int[0];
    }

    @Override
    public Iterator<Integer> iterator() {
        return Arrays.asList(ArrayUtils.toObject(this.value)).iterator();
    }

    @Override
    public void forEach(Consumer<? super Integer> action) {
        Arrays.asList(ArrayUtils.toObject(this.value)).forEach(action);
    }

    @Override
    public Spliterator<Integer> spliterator() {
        return Arrays.asList(ArrayUtils.toObject(this.value)).spliterator();
    }

    @Override
    public IntArrayTag copy() {
        return new IntArrayTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof IntArrayTag other && this.value == other.value);
    }
}
