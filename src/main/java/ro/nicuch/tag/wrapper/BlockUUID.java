package ro.nicuch.tag.wrapper;

import org.bukkit.block.Block;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record BlockUUID(int x, int y, int z) {

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

    public static BlockUUID fromCoord(int x, int y, int z) {
        return new BlockUUID(x, y, z);
    }

    public static BlockUUID fromBlock(Block block) {
        return new BlockUUID(block.getY(), block.getY(), block.getZ());
    }

    public static BlockUUID fromString(String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                return new BlockUUID(x, y, z);
            } else
                throw new IllegalArgumentException("BlockUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("BlockUUID couldn't parse from string.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockUUID that = (BlockUUID) o;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public String toString() {
        return "<x" + this.x + ",y" + this.y + ",z" + this.z + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
