package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

/**
 * A tag representing a {@code double}.
 */
public final class DoubleTag implements NumberTag {
    /**
     * The double value.
     */
    private double value;

    DoubleTag() {
    }

    public DoubleTag(final double value) {
        this.value = value;
    }

    @Override
    public byte byteValue() {
        return (byte) (NumberTag.floor(this.value) & 0xff);
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
        return NumberTag.floor(this.value);
    }

    @Override
    public long longValue() {
        return (long) Math.floor(this.value);
    }

    @Override
    public short shortValue() {
        return (short) (NumberTag.floor(this.value) & 0xffff);
    }

    @Override
    public int bufferDataSize() {
        return 8;
    }

    @Override
    public DoubleTag read(ByteBuffer input, int depth) {
        this.value = input.getDouble();
        return this;
    }

    @Override
    public DoubleTag write(ByteBuffer output) {
        output.putDouble(this.value);
        return this;
    }

    @Override
    public TagType type() {
        return TagType.DOUBLE;
    }

    @Override
    public DoubleTag copy() {
        return new DoubleTag(this.value);
    }

    @Override
    public int hashCode() {
        return Double.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof DoubleTag && Double.doubleToLongBits(this.value) == Double.doubleToLongBits(((DoubleTag) that).value));
    }
}
