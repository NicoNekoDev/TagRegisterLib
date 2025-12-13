package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

/**
 * A tag representing a {@code byte}.
 */
public final class ByteTag implements NumberTag {
    /**
     * A {@code byte} representing a {@code boolean} value of {@code false}.
     */
    static final byte FALSE = 0;
    /**
     * A {@code byte} representing a {@code boolean} value of {@code true}.
     */
    static final byte TRUE = 1;
    /**
     * The byte value.
     */
    private byte value;

    public ByteTag() {
    }

    public ByteTag(final byte value) {
        this.value = value;
    }

    @Override
    public byte byteValue() {
        return this.value;
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
        return 1;
    }

    @Override
    public ByteTag read(ByteBuffer input, int depth) {
        this.value = input.get();
        return this;
    }

    @Override
    public ByteTag write(ByteBuffer output) {
        output.put(this.value);
        return this;
    }

    @Override
    public TagType type() {
        return TagType.BYTE;
    }

    @Override
    public ByteTag copy() {
        return new ByteTag(this.value);
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ByteTag && this.value == ((ByteTag) that).value);
    }
}
