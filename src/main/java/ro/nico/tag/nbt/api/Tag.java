package ro.nico.tag.nbt.api;

import ro.nico.tag.nbt.tags.TagType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * An abstract NBT tag.
 *
 * @author dewy
 */
public abstract class Tag {
    private String name;

    /**
     * Returns the name (key) of this tag.
     *
     * @return the name (key) of this tag.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name (key) of this tag.
     *
     * @param name the new name to be set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a unique ID for this NBT tag type. 0 to 12 (inclusive) are reserved.
     *
     * @return a unique ID for this NBT tag type.
     */
    public abstract TagType getType();

    /**
     * Returns the value held by this tag.
     *
     * @return the value held by this tag.
     */
    public abstract Object getValue();

    /**
     * Writes this tag to a {@link DataOutput} stream.
     *
     * @param output   the stream to write to.
     * @param depth    the current depth of the NBT data structure.
     * @throws IOException if any I/O error occurs.
     */
    public abstract Tag write(DataOutput output, int depth) throws IOException;

    /**
     * Reads this tag from a {@link DataInput} stream.
     *
     * @param input    the stream to read from.
     * @param depth    the current depth of the NBT data structure.
     * @return this (literally {@code return this;} after reading).
     * @throws IOException if any I/O error occurs.
     */
    public abstract Tag read(DataInput input, int depth) throws IOException;

    /**
     * Creates a copy of this tag.
     *
     * @return a copy of this tag
     */
    public abstract Tag copy();
}
