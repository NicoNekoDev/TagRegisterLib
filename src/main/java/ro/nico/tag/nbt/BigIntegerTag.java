package ro.nico.tag.nbt;

import java.math.BigInteger;
import java.nio.ByteBuffer;

/**
 * A tag representing an {@code big integer}.
 */
public class BigIntegerTag implements NumberTag {
    /**
     * The big int value.
     */
    private BigInteger value;

    public BigIntegerTag() {
    }

    public BigIntegerTag(final BigInteger value) {
        this.value = value;
    }

    public BigInteger value() {
        return this.value;
    }

    @Override
    public byte byteValue() {
        return this.value.byteValue();
    }

    @Override
    public double doubleValue() {
        return this.value.doubleValue();
    }

    @Override
    public float floatValue() {
        return this.value.floatValue();
    }

    @Override
    public int intValue() {
        return this.value.intValue();
    }

    @Override
    public long longValue() {
        return this.value.longValue();
    }

    @Override
    public short shortValue() {
        return this.value.shortValue();
    }

    @Override
    public int bufferDataSize() {
        return this.value.toByteArray().length;
    }

    @Override
    public BigIntegerTag read(ByteBuffer input, int depth) {
        final int length = input.getInt();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++)
            bytes[i] = input.get();
        this.value = new BigInteger(bytes);
        return this;
    }

    @Override
    public BigIntegerTag write(ByteBuffer output) {
        byte[] bytes = this.value.toByteArray();
        output.putInt(bytes.length);
        for (byte b : bytes)
            output.put(b);
        return this;
    }

    @Override
    public TagType type() {
        return TagType.BIG_INT;
    }

    @Override
    public BigIntegerTag copy() {
        return new BigIntegerTag(this.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof BigIntegerTag && this.value.equals(((BigIntegerTag) that).value));
    }
}