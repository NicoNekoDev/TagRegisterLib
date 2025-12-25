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
 * The double tag (type ID 6) is used for storing a double-precision 64-bit IEEE 754 floating point value; a Java primitive {@code double}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class DoubleTag extends NumericalTag<Double> {
    private double value;

    /**
     * Constructs a double tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code double} value.
     */
    public DoubleTag(String name, double value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.DOUBLE;
    }

    @Override
    public Double getValue() {
        return this.value;
    }

    /**
     * Sets the {@code double} value of this double tag.
     *
     * @param value new {@code double} value to be set.
     */
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public DoubleTag write(DataOutput output, int depth) throws IOException {
        output.writeDouble(this.value);

        return this;
    }

    @Override
    public DoubleTag read(DataInput input, int depth) throws IOException {
        this.value = input.readDouble();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return this.value + "d";
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
    public DoubleTag fromJson(JsonObject json, int depth) {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsDouble();

        return this;
    }

    @Override
    public Tag copy() {
        return new DoubleTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Double.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof DoubleTag other && this.value == other.value);
    }
}
