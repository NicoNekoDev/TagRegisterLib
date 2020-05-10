package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
    public void read(final DataInput input, final int depth) throws IOException {
        this.value = input.readFloat();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeFloat(this.value);
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
