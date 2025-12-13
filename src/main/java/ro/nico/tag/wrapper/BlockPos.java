package ro.nico.tag.wrapper;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record BlockPos(byte x, byte y, byte z) {

    private final static Pattern pattern = Pattern.compile("<x(-?[0-9]+),y(-?[0-9]+),z(-?[0-9]+)>");

    public byte getX() {
        return this.x;
    }

    public byte getY() {
        return this.y;
    }

    public byte getZ() {
        return this.z;
    }

    public int bufferDataSize() {
        return 3;
    }

    public BlockPos to(ByteBuffer buffer) {
        buffer.put(this.x).put(this.y).put(this.z);
        return this;
    }

    public static BlockPos fromLocation(int x, int y, int z) {
        return new BlockPos((byte) (x % 16), (byte) (y % 16), (byte) (z % 16));
    }

    public static BlockPos from(ByteBuffer buffer) {
        return new BlockPos(buffer.get(), buffer.get(), buffer.get());
    }

    public static BlockPos fromString(String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                byte x = Byte.parseByte(matcher.group(1));
                byte y = Byte.parseByte(matcher.group(2));
                byte z = Byte.parseByte(matcher.group(3));
                return new BlockPos(x, y, z);
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
        BlockPos that = (BlockPos) o;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @NotNull
    @Override
    public String toString() {
        return "<x" + this.x + ",y" + this.y + ",z" + this.z + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
