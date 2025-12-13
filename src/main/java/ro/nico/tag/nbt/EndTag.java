package ro.nico.tag.nbt;

import java.nio.ByteBuffer;

/**
 * An end tag.
 */
public final class EndTag implements Tag {
    public EndTag() {
    }

    @Override
    public int bufferDataSize() {
        return 0;
    }

    @Override
    public EndTag read(ByteBuffer input, int depth) {
        return this;
    }

    @Override
    public EndTag write(ByteBuffer output) {
        return this;
    }

    @Override
    public TagType type() {
        return TagType.END;
    }

    @Override
    public EndTag copy() {
        return new EndTag(); // end tags have no data
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
