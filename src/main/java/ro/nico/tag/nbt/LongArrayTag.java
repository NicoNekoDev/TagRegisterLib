package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * A tag representing an array of {@code long}s.
 */
public final class LongArrayTag extends AbstractList<LongTag> implements IndexedCollectionTag<LongTag> {
    /**
     * The array of longs.
     */
    private long[] value;

    public LongArrayTag() {
    }

    public LongArrayTag(final long[] value) {
        this.value = value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public LongTag get(final int index) {
        return new LongTag(this.value[index]);
    }

    /**
     * Gets the array of longs.
     *
     * @return the array of longs
     */
    public long[] value() {
        return this.value;
    }

    @Override
    public int bufferDataSize() {
        return 8 * this.value.length;
    }

    @Override
    public LongArrayTag read(ByteBuffer input, int depth) {
        final int length = input.getInt();
        this.value = new long[length];
        for (int i = 0; i < length; i++) {
            this.value[i] = input.getLong();
        }
        return this;
    }

    @Override
    public LongArrayTag write(ByteBuffer output) {
        output.putInt(this.value.length);
        for (long l : this.value)
            output.putLong(l);
        return this;
    }

    @Override
    public TagType type() {
        return TagType.LONG_ARRAY;
    }

    @Override
    public LongArrayTag copy() {
        final long[] value = new long[this.value.length];
        System.arraycopy(this.value, 0, value, 0, this.value.length);
        return new LongArrayTag(value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof LongArrayTag && Arrays.equals(this.value, ((LongArrayTag) that).value));
    }
}
