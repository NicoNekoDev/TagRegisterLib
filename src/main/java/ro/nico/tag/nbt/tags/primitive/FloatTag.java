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
 * The float tag (type ID 5) is used for storing a single-precision 32-bit IEEE 754 floating point value; a Java primitive {@code float}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class FloatTag extends NumericalTag<Float> {
    private float value;

    /**
     * Constructs a float tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code float} value.
     */
    public FloatTag(String name, float value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.FLOAT;
    }

    @Override
    public Float getValue() {
        return this.value;
    }

    /**
     * Sets the {@code float} value of this float tag.
     *
     * @param value new {@code float} value to be set.
     */
    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public FloatTag write(DataOutput output, int depth) throws IOException {
        output.writeFloat(this.value);

        return this;
    }

    @Override
    public FloatTag read(DataInput input, int depth) throws IOException {
        this.value = input.readFloat();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return this.value + "f";
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
    public FloatTag fromJson(JsonObject json, int depth) {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsFloat();

        return this;
    }

    @Override
    public Tag copy() {
        return new FloatTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Float.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof FloatTag other && this.value == other.value);
    }
}
