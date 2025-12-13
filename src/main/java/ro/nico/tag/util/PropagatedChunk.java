package ro.nico.tag.util;

import ro.nico.tag.wrapper.ChunkPos;

import java.util.Objects;

public record PropagatedChunk(ChunkPos pos, Direction direction, PropagationType propagationType, int distance) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropagatedChunk that = (PropagatedChunk) o;
        return Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }
}
