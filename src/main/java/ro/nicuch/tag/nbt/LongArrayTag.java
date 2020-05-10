package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
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
    public void read(final DataInput input, final int depth) throws IOException {
        final int length = input.readInt();
        this.value = new long[length];
        for (int i = 0; i < length; i++) {
            this.value[i] = input.readLong();
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        for (long l : this.value) {
            output.writeLong(l);
        }
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
