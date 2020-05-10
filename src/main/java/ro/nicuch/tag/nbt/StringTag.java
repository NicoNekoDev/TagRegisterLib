package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A tag representing a {@link String}.
 */
public final class StringTag implements Tag {
    private String value;

    public StringTag() {
    }

    public StringTag(final String value) {
        this.value = requireNonNull(value, "value");
    }

    public String value() {
        return this.value;
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        this.value = input.readUTF();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeUTF(this.value);
    }

    @Override
    public TagType type() {
        return TagType.STRING;
    }

    @Override
    public StringTag copy() {
        return new StringTag(this.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof StringTag && Objects.equals(this.value, ((StringTag) that).value));
    }
}
