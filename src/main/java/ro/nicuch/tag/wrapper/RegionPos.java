package ro.nicuch.tag.wrapper;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RegionPos(int x, int y, int z) {

    private final static Pattern pattern = Pattern.compile("<x([-]?[0-9]+),y([-]?[0-9]+),z([-]?[0-9]+)>");

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public static RegionPos fromString(final String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                return new RegionPos(x, y, z);
            } else
                throw new IllegalArgumentException("RegionUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("RegionUUID couldn't parse from string.");
        }
    }

    public static RegionPos fromChunk(ChunkPos id) {
        int regX = id.getX() >> 5;
        int regY = id.getY() >> 5;
        int regZ = id.getZ() >> 5;
        return new RegionPos(regX, regY, regZ);
    }

    @Override
    public String toString() {
        return "<x" + this.x + ",y" + this.y + ",z" + this.z + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionPos that = (RegionPos) o;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
