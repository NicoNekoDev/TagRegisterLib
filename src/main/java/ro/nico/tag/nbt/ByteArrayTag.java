package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
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
    public int bufferDataSize() {
        return this.value.length;
    }

    @Override
    public ByteArrayTag read(ByteBuffer input, int depth) {
        final int length = input.getInt();
        this.value = new byte[length];
        for (int i = 0; i < length; i++)
            this.value[i] = input.get();
        return this;
    }

    @Override
    public ByteArrayTag write(ByteBuffer output) {
        output.putInt(this.value.length);
        for (byte b : this.value)
            output.put(b);
        return this;
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
