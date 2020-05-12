package ro.nicuch.tag.wrapper;

import org.bukkit.block.Block;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockUUID {
    private final int x;
    private final int y;
    private final int z;

    private final static Pattern pattern = Pattern.compile("x([-]?[0-9]+),y([-]?[0-9]+),z([-]?[0-9]+)");

    public BlockUUID(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockUUID(final Block block) {
        this(block.getX(), block.getY(), block.getZ());
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
    public boolean equals(Object obj) {
        if (this != obj)
            return false;
        if (!(obj instanceof BlockUUID))
            return false;
        BlockUUID that = (BlockUUID) obj;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public String toString() {
        return "x" + x + ",y" + y + ",z" + z + "";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
