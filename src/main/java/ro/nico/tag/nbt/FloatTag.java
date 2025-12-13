package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

/**
 * A tag representing a {@code float}.
 */
public final class FloatTag implements NumberTag {
    /**
     * The float value.
     */
    private float value;

    public FloatTag() {
    }

    public FloatTag(final float value) {
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
        return this.value;
    }

    @Override
    public int intValue() {
        return NumberTag.floor(this.value);
    }

    @Override
    public long longValue() {
        return (long) this.value;
    }

    @Override
    public short shortValue() {
        return (short) (NumberTag.floor(this.value) & 0xffff);
    }

    @Override
    public int bufferDataSize() {
        return 4;
    }

    @Override
    public FloatTag read(ByteBuffer input, int depth) {
        this.value = input.getFloat();
        return this;
    }

    @Override
    public FloatTag write(ByteBuffer output) {
        output.putFloat(this.value);
        return this;
    }

    @Override
    public TagType type() {
        return TagType.FLOAT;
    }

    @Override
    public FloatTag copy() {
        return new FloatTag(this.value);
    }

    @Override
    public int hashCode() {
        return Float.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof FloatTag && Float.floatToIntBits(this.value) == Float.floatToIntBits(((FloatTag) that).value));
    }
}
