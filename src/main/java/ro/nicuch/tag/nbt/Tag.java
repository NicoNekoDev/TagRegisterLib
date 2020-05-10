package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A tag.
 */
public interface Tag {
    /**
     * Reads the value of this tag from {@code input}.
     *
     * @param input the input
     * @param depth the depth
     * @throws IOException if an exception was encountered while reading
     */
    void read(final DataInput input, final int depth) throws IOException;

    /**
     * Writes the value of this tag to {@code output}.
     *
     * @param output the output
     * @throws IOException if an exception was encountered while writing
     */
    void write(final DataOutput output) throws IOException;

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
