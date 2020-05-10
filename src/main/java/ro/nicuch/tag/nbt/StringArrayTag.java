package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;

/**
 * A tag representing an array of {@code string}s.
 */
public final class StringArrayTag extends AbstractList<StringTag> implements IndexedCollectionTag<StringTag> {
    /**
     * The array of strings.
     */
    private String[] value;

    public StringArrayTag() {
    }

    public StringArrayTag(final String[] value) {
        this.value = value;
    }

    @Override
    public int size() {
        return this.value.length;
    }

    @Override
    public StringTag get(final int index) {
        return new StringTag(this.value[index]);
    }

    /**
     * Gets the array of strings.
     *
     * @return the array of strings
     */
    public String[] value() {
        return this.value;
    }

    @Override
    public void read(final DataInput input, final int depth) throws IOException {
        final int length = input.readInt();
        this.value = new String[length];
        for (int i = 0; i < length; i++) {
            this.value[i] = input.readUTF();
        }
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        output.writeInt(this.value.length);
        for (String s : this.value) {
            output.writeUTF(s);
        }
    }

    @Override
    public TagType type() {
        return TagType.STRING_ARRAY;
    }

    @Override
    public StringArrayTag copy() {
        final String[] value = new String[this.value.length];
        System.arraycopy(this.value, 0, value, 0, this.value.length);
        return new StringArrayTag(value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof StringArrayTag && Arrays.equals(this.value, ((StringArrayTag) that).value));
    }
}