package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A tag representing a {@code long}.
 */
public final class LongTag implements NumberTag {
    /**
     * The long value.
     */
    private long value;

    public LongTag() {
    }

    public LongTag(final long value) {
        this.value = value;
    }

    @Override
    public byte byteValue() {
        return (byte) (this.value & 0xff);
    }

    @Override
    public double doubleValue() {
        return (double) this.value;
    }

    @Override
    public float floatValue() {
        return (float) this.value;
    }

    @Override
    public int intValue() {
        return Math.toIntExact(this.value);
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
        this.value = input.readLong();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeLong(this.value);
    }

    @Override
    public TagType type() {
        return TagType.LONG;
    }

    @Override
    public LongTag copy() {
        return new LongTag(this.value);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof LongTag && this.value == ((LongTag) that).value);
    }
}
