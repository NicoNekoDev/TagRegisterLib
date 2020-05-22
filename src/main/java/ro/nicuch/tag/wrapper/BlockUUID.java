package ro.nicuch.tag.wrapper;

import org.bukkit.block.Block;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockUUID {
    private final byte x;
    private final byte y;
    private final byte z;

    private final static Pattern pattern = Pattern.compile("x([-]?[0-9]+),y([-]?[0-9]+),z([-]?[0-9]+)");

    public BlockUUID(final byte x, final byte y, final byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockUUID(final Block block) {
        this((byte) Math.floorMod(block.getX(), 16), (byte) ((byte) block.getY() - 128), (byte) Math.floorMod(block.getZ(), 16));
    }

    public final byte getX() {
        return this.x;
    }

    public final byte getY() {
        return this.y;
    }

    public final byte getZ() {
        return this.z;
    }


    public static BlockUUID fromString(String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                byte x = Byte.parseByte(matcher.group(1));
                byte y = Byte.parseByte(matcher.group(2));
                byte z = Byte.parseByte(matcher.group(3));
                return new BlockUUID(x, y, z);
            } else
                throw new IllegalArgumentException("BlockUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("BlockUUID couldn't parse from string.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
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
        return Objects.hash(x, y, z);
    }
}
