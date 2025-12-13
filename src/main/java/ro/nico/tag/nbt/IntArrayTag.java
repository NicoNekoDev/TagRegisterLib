package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * A tag representing an array of {@code int}s.
 */
public final class IntArrayTag extends AbstractList<IntTag> implements IndexedCollectionTag<IntTag> {
    /**
     * The array of ints.
     */
    private int[] value;

    public IntArrayTag() {
    }

    public IntArrayTag(final int[] value) {
        this.value = value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public IntTag get(final int index) {
        return new IntTag(this.value[index]);
    }

    /**
     * Gets the array of ints.
     *
     * @return the array of ints
     */
    public int[] value() {
        return this.value;
    }

    @Override
    public int bufferDataSize() {
        return 4 * this.value.length;
    }

    @Override
    public IntArrayTag read(ByteBuffer input, int depth) {
        final int length = input.getInt();
        this.value = new int[length];
        for (int i = 0; i < length; i++)
            this.value[i] = input.getInt();
        return this;
    }

    @Override
    public IntArrayTag write(ByteBuffer output) {
        output.putInt(this.value.length);
        for (int i : this.value)
            output.putInt(i);
        return this;
    }

    @Override
    public TagType type() {
        return TagType.INT_ARRAY;
    }

    @Override
    public IntArrayTag copy() {
        final int[] value = new int[this.value.length];
        System.arraycopy(this.value, 0, value, 0, this.value.length);
        return new IntArrayTag(value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof IntArrayTag && Arrays.equals(this.value, ((IntArrayTag) that).value));
    }
}
