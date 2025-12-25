package ro.nico.tag.nbt.tags.primitive;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ro.nico.tag.nbt.api.Tag;
import ro.nico.tag.nbt.api.json.JsonSerializable;
import ro.nico.tag.nbt.api.snbt.SnbtConfig;
import ro.nico.tag.nbt.api.snbt.SnbtSerializable;
import ro.nico.tag.nbt.tags.TagType;
import ro.nico.tag.nbt.utils.StringUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The string tag (type ID 8) is used for storing a UTF-8 encoded {@code String}, prefixed by a length value stored as a 32-bit {@code int}.
 *
 * @author dewy
 */
@NoArgsConstructor
@AllArgsConstructor
public class StringTag extends Tag implements SnbtSerializable, JsonSerializable {
    private @NotNull String value;

    /**
     * Constructs a string tag with a given name and value.
     *
     * @param name  the tag's name.
     * @param value the tag's {@code String} value.
     */
    public StringTag(String name, @NotNull String value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public TagType getType() {
        return TagType.STRING;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the {@code String} value of this string tag.
     *
     * @param value new {@code String} value to be set.
     */
    public void setValue(@NotNull String value) {
        this.value = value;
    }

    @Override
    public StringTag write(DataOutput output, int depth) throws IOException {
        output.writeUTF(this.value);

        return this;
    }

    @Override
    public StringTag read(DataInput input, int depth) throws IOException {
        this.value = input.readUTF();

        return this;
    }

    @Override
    public String toSnbt(int depth, SnbtConfig config) {
        return StringUtils.escapeSnbt(this.value);
    }

    @Override
    public JsonObject toJson(int depthy) {
        JsonObject json = new JsonObject();
        json.addProperty("type", this.getType().getName());

        if (this.getName() != null) {
            json.addProperty("name", this.getName());
        }

        json.addProperty("value", this.value);

        return json;
    }

    @Override
    public StringTag fromJson(JsonObject json, int depth) {
        if (json.has("name")) {
            this.setName(json.getAsJsonPrimitive("name").getAsString());
        } else {
            this.setName(null);
        }

        this.value = json.getAsJsonPrimitive("value").getAsString();

        return this;
    }

    @Override
    public String toString() {
        return this.toSnbt(0, new SnbtConfig());
    }

    @Override
    public StringTag copy() {
        return new StringTag(getName(), getValue());
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof StringTag other && this.value.equals(other.value));
    }
}
