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
 * The byte tag (type ID 1) is used for storing an 8-bit signed two's complement integer; a Java primitive {@code byte}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class ByteTag extends NumericalTag<Byte> {
    private byte value;

    /**
     * Constructs a byte tag with a given value.
     *
     * @param value the tag's {@code Number} value, to be converted to {@code byte}.
     */
    public ByteTag(@NotNull Number value) {
        this(null, value);
    }

    /**
     * Constructs a byte tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code Number} value, to be converted to {@code byte}.
     */
    public ByteTag(String name, @NotNull Number value) {
        this(name, value.byteValue());
    }

    /**
     * Constructs a byte tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code byte} value.
     */
    public ByteTag(String name, byte value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.BYTE;
    }

    @Override
    public Byte getValue() {
        return this.value;
    }

    /**
     * Sets the {@code byte} value of this byte tag.
     *
     * @param value new {@code byte} value to be set.
     */
    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public ByteTag write(DataOutput output, int depth) throws IOException {
        output.writeByte(this.value);

        return this;
    }

    @Override
    public ByteTag read(DataInput input, int depth) throws IOException {
        this.value = input.readByte();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return this.value + "b";
    }

    @Override
    public JsonObject toJson(int depth) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.getType().getName());

        if (this.getName() != null) {
            json.addProperty("name", this.getName());
        }

        json.addProperty("value", this.value);

        return json;
    }

    @Override
    public ByteTag fromJson(JsonObject json, int depth) throws IOException {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsByte();

        return this;
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ByteTag other && this.value == other.value);
    }
}
