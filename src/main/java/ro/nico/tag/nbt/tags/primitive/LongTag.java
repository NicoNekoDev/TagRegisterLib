package ro.nico.tag.nbt.tags.primitive;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ro.nico.tag.nbt.api.snbt.SnbtConfig;
import ro.nico.tag.nbt.tags.TagType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The long tag (type ID 4) is used for storing a 64-bit signed two's complement integer; a Java primitive {@code long}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class LongTag extends NumericalTag<Long> {
    private long value;

    /**
     * Constructs a long tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code long} value.
     */
    public LongTag(String name, long value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.LONG;
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    /**
     * Sets the {@code long} value of this long tag.
     *
     * @param value new {@code long} value to be set.
     */
    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public LongTag write(DataOutput output, int depth) throws IOException {
        output.writeLong(this.value);

        return this;
    }

    @Override
    public LongTag read(DataInput input, int depth) throws IOException {
        this.value = input.readLong();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return this.value + "L";
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
    public LongTag fromJson(JsonObject json, int depth) {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsLong();

        return this;
    }

    @Override
    public LongTag copy() {
        return new LongTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof LongTag other && this.value == other.value);
    }
}
