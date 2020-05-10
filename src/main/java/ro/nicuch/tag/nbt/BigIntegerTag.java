package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;

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
    public void read(final DataInput input, final int depth) throws IOException {
        final int length = input.readInt();
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        this.value = new BigInteger(bytes);
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        byte[] bytes = this.value.toByteArray();
        output.writeInt(bytes.length);
        output.write(bytes);
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