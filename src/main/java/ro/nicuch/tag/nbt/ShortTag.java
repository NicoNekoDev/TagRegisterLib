package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A tag representing a {@code short}.
 */
public final class ShortTag implements NumberTag {
    /**
     * The short value.
     */
    private short value;

    public ShortTag() {
    }

    public ShortTag(final short value) {
        this.value = value;
    }

    @Override
    public byte byteValue() {
        return (byte) (this.value & 0xff);
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return this.value;
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        this.value = input.readShort();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeShort(this.value);
    }

    @Override
    public TagType type() {
        return TagType.SHORT;
    }

    @Override
    public ShortTag copy() {
        return new ShortTag(this.value);
    }

    @Override
    public int hashCode() {
        return Short.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ShortTag && this.value == ((ShortTag) that).value);
    }
}
