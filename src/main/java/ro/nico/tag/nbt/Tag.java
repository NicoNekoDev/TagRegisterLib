package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

/**
 * A tag.
 */
public interface Tag {

    int bufferDataSize();

    /**
     * Reads the value of this tag from {@code input}.
     *
     * @param input the input
     * @param depth the depth
     *
     * @return itself
     */
    Tag read(final ByteBuffer input, final int depth);

    /**
     * Writes the value of this tag to {@code output}.
     *
     * @param output the output
     *
     * @return itself
     */
    Tag write(final ByteBuffer output);

    /**
     * Gets the type of this tag.
     *
     * @return the type
     */
    TagType type();

    /**
     * Creates a copy of this tag.
     *
     * @return a copy of this tag
     */
    Tag copy();

    @Override
    int hashCode();

    @Override
    boolean equals(final Object that);
}
