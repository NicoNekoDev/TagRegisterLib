package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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
    public void read(final DataInput input, final int depth) throws IOException {
        final int length = input.readInt();
        this.value = new int[length];
        for (int i = 0; i < length; i++) {
            this.value[i] = input.readInt();
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        for (int i1 : this.value) {
            output.writeInt(i1);
        }
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
