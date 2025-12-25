package ro.nico.tag.nbt.tags.primitive;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ro.nico.tag.nbt.api.snbt.SnbtConfig;
import ro.nico.tag.nbt.tags.TagType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The short tag (type ID 2) is used for storing a 16-bit signed two's complement integer; a Java primitive {@code short}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class ShortTag extends NumericalTag<Short> {
    private short value;

    /**
     * Constructs a short tag with a given value.
     *
     * @param value the tag's {@code Number} value, to be converted to {@code short}.
     */
    public ShortTag(@NotNull Number value) {
        this(null, value);
    }

    /**
     * Constructs a short tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code Number} value, to be converted to {@code short}.
     */
    public ShortTag(String name, @NotNull Number value) {
        this(name, value.shortValue());
    }

    /**
     * Constructs a short tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code short} value.
     */
    public ShortTag(String name, short value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.SHORT;
    }

    @Override
    public Short getValue() {
        return this.value;
    }

    /**
     * Sets the {@code short} value of this short tag.
     *
     * @param value new {@code short} value to be set.
     */
    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public ShortTag write(DataOutput output, int depth) throws IOException {
        output.writeShort(this.value);

        return this;
    }

    @Override
    public ShortTag read(DataInput input, int depth) throws IOException {
        this.value = input.readShort();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return this.value + "s";
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
    public ShortTag fromJson(JsonObject json, int depth) {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsShort();

        return this;
    }

    @Override
    public ShortTag copy() {
        return new ShortTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Short.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ShortTag other && this.value == other.value);
    }
}
