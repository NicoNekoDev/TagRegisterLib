package ro.nicuch.tag.wrapper;

import org.bukkit.World;
import org.bukkit.block.Block;
import ro.nicuch.tag.grid.Direction;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ChunkUUID(int x, int y, int z) {

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

    public ChunkUUID getRelative(Direction direction) {
        return new ChunkUUID(this.x + direction.getModX(), this.y + direction.getModY(), this.z + direction.getModZ());
    }

    public int getWorkerThread(int threads) {
        int gridX = this.x % threads;
        int gridY = this.y % threads;
        int gridZ = this.z % threads;

        if (gridX < 0)
            gridX += threads;
        if (gridY < 0)
            gridY += threads;
        if (gridZ < 0)
            gridZ += threads;
        return (gridX + gridY + gridZ) % threads;
    }

    public static ChunkUUID fromString(final String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                return new ChunkUUID(x, y, z);
            } else
                throw new IllegalArgumentException("ChunkUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("ChunkUUID couldn't parse from string.");
        }
    }

    public static ChunkUUID fromLocation(Block block) {
        return new ChunkUUID(block.getX(), block.getY(), block.getZ());
    }

    public static Block toLocation(ChunkUUID uuid, World world) {
        return world.getBlockAt(uuid.getX(), uuid.getY(), uuid.getZ());
    }

    @Override
    public String toString() {
        return "<x" + this.x + ",y" + this.y + ",z" + this.z + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkUUID that = (ChunkUUID) o;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
