package ro.nico.tag.nbt;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A tag representing a {@link String}.
 */
public final class StringTag implements Tag {
    private String value;

    public StringTag() {
    }

    public StringTag(final String value) {
        this.value = requireNonNull(value, "value");
    }

    public String value() {
        return this.value;
    }

    @Override
    public int bufferDataSize() {
        return this.value.length() * 2;
    }

    @Override
    public StringTag read(final ByteBuffer input, final int depth) {
        int size = input.getInt();
        char[] arr = new char[size];
        for (int i = 0; i < size; i++)
            arr[i] = input.getChar();
        this.value = new String(arr);
        return this;
    }

    @Override
    public StringTag write(final ByteBuffer output) {
        char[] arr = this.value.toCharArray();
        output.putInt(arr.length);
        for (char ch : arr)
            output.putChar(ch);
        return this;
    }

    public void write(final CharBuffer outputBuffer) {
        outputBuffer.put(this.value);
    }

    @Override
    public TagType type() {
        return TagType.STRING;
    }

    @Override
    public StringTag copy() {
        return new StringTag(this.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return this == that || (that instanceof StringTag && Objects.equals(this.value, ((StringTag) that).value));
    }
}
