package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

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
    public int bufferDataSize() {
        return 8;
    }

    @Override
    public LongTag read(ByteBuffer input, int depth) {
        this.value = input.getLong();
        return this;
    }

    @Override
    public LongTag write(ByteBuffer output) {
        output.putLong(this.value);
        return this;
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
