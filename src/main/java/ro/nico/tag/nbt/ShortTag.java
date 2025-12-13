package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

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
    public int bufferDataSize() {
        return 2;
    }

    @Override
    public ShortTag read(ByteBuffer input, int depth) {
        this.value = input.getShort();
        return this;
    }

    @Override
    public ShortTag write(ByteBuffer output) {
        output.putShort(this.value);
        return this;
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
