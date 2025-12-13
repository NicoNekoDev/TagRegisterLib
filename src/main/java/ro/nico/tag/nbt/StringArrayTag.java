package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
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
    public int bufferDataSize() {
        return Arrays.stream(this.value).mapToInt(str -> str.length() * 2).sum();
    }

    @Override
    public StringArrayTag read(ByteBuffer input, int depth) {
        final int length = input.getInt();
        this.value = new String[length];
        for (int i = 0; i < length; i++) {
            final int strLength = input.getInt();
            final char[] strArr = new char[strLength];
            for (int n = 0; n < strLength; n++) {
                strArr[n] = input.getChar();
            }
            this.value[i] = new String(strArr);
        }
        return this;
    }

    @Override
    public StringArrayTag write(ByteBuffer output) {
        output.putInt(this.value.length);
        for (String str : this.value) {
            char[] strArr = str.toCharArray();
            output.putInt(str.length());
            for (char ch : strArr)
                output.putChar(ch);
        }
        return this;
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