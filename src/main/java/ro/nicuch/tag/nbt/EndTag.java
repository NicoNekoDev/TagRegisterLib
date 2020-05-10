package ro.nicuch.tag.nbt;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * An end tag.
 */
public final class EndTag implements Tag {
    public EndTag() {
    }

    @Override
    public void read(final DataInput input, final int depth) {
    }

    @Override
    public void write(final DataOutput output) {
    }

    @Override
    public TagType type() {
        return TagType.END;
    }

    @Override
    public EndTag copy() {
        return this; // end tags have no data
    }

    @Override
    public int hashCode() {
        return TagType.END.hashCode();
    }

    @Override
    public boolean equals(final Object that) {
        return that instanceof EndTag;
    }
}
