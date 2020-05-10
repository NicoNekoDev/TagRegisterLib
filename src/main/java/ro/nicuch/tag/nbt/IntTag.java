package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A tag representing an {@code int}.
 */
public final class IntTag implements NumberTag {
    /**
     * The int value.
     */
    private int value;

    public IntTag() {
    }

    public IntTag(final int value) {
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
        return (float) this.value;
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
        return (short) (this.value & 0xffff);
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        this.value = input.readInt();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(this.value);
    }

    @Override
    public TagType type() {
        return TagType.INT;
    }

    @Override
    public IntTag copy() {
        return new IntTag(this.value);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof IntTag && this.value == ((IntTag) that).value);
    }
}
