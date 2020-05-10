package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
    public void read(final DataInput input, final int depth) throws IOException {
        this.value = input.readByte();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeByte(this.value);
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
