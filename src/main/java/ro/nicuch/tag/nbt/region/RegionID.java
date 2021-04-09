package ro.nicuch.tag.nbt.region;

import java.util.Objects;

public class RegionID {

    private final int x;
    private final int y;
    private final int z;

    public RegionID(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final int getX() {
        return this.x;
    }

    public final int getY() {
        return this.y;
    }

    public final int getZ() {
        return this.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionID that = (RegionID) o;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }

    public static RegionID fromChunk(final int chunk_x, final int chunk_y, final int chunk_z) {
        return new RegionID(chunk_x >> 5, chunk_y >> 5, chunk_z >> 5);
    }
}
