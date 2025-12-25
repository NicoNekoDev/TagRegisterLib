package ro.nico.tag.wrapper;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ro.nico.tag.util.Direction;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionPos {
    @Getter
    private final int x, y, z;

    private RegionPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private final static Pattern pattern = Pattern.compile("<x([-]?[0-9]+),y([-]?[0-9]+),z([-]?[0-9]+)>");

    public RegionPos getRelative(Direction direction) {
        return new RegionPos(this.x + direction.getModX(), this.y + direction.getModY(), this.z + direction.getModZ());
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

    @NotNull
    @Override
    public String toString() {
        return "<x" + this.x + ",y" + this.y + ",z" + this.z + ">";
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof RegionPos other && this.x == other.x && this.y == other.y && this.z == other.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
