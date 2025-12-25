package ro.nico.tag.nbt.tags.primitive;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ro.nico.tag.nbt.api.Tag;
import ro.nico.tag.nbt.api.snbt.SnbtConfig;
import ro.nico.tag.nbt.tags.TagType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The int tag (type ID 3) is used for storing a 32-bit signed two's complement integer; a Java primitive {@code int}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class IntTag extends NumericalTag<Integer> {
    private int value;

    /**
     * Constructs an int tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code int} value.
     */
    public IntTag(String name, int value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.INT;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    /**
     * Sets the {@code int} value of this int tag.
     *
     * @param value new {@code int} value to be set.
     */
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public IntTag write(DataOutput output, int depth) throws IOException {
        output.writeInt(this.value);

        return this;
    }

    @Override
    public IntTag read(DataInput input, int depth) throws IOException {
        this.value = input.readInt();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return Integer.toString(this.value);
    }

    @Override
    public JsonObject toJson(int depth) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.getType().getName());

        if (this.getName() != null) {
            json.addProperty("name", this.getName());
        }

        json.addProperty("value", this.value);

        return json;
    }

    @Override
    public IntTag fromJson(JsonObject json, int depth) {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsInt();

        return this;
    }

    @Override
    public Tag copy() {
        return new IntTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof IntTag other && this.value == other.value);
    }
}
