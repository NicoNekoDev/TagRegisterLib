package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

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
    public int bufferDataSize() {
        return 4;
    }

    @Override
    public IntTag read(ByteBuffer input, int depth) {
        this.value = input.getInt();
        return this;
    }

    @Override
    public IntTag write(ByteBuffer output) {
        output.putInt(this.value);
        return this;
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
