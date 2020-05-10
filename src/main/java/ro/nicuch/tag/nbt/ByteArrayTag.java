package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * A tag representing an array of {@code byte}s.
 */
public final class ByteArrayTag extends AbstractList<ByteTag> implements IndexedCollectionTag<ByteTag> {
    /**
     * The array of bytes.
     */
    private byte[] value;

    public ByteArrayTag() {
    }

    public ByteArrayTag(final byte[] value) {
        this.value = value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public ByteTag get(final int index) {
        return new ByteTag(this.value[index]);
    }

    /**
     * Gets the array of bytes.
     *
     * @return the array of bytes
     */
    public byte[] value() {
        return this.value;
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        final int length = input.readInt();
        this.value = new byte[length];
        input.readFully(this.value);
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        output.write(this.value);
    }

    @Override
    public TagType type() {
        return TagType.BYTE_ARRAY;
    }

    @Override
    public ByteArrayTag copy() {
        final byte[] value = new byte[this.value.length];
        System.arraycopy(this.value, 0, value, 0, this.value.length);
        return new ByteArrayTag(value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof ByteArrayTag && Arrays.equals(this.value, ((ByteArrayTag) that).value));
    }
}
