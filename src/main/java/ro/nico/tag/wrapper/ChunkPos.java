package ro.nico.tag.wrapper;

import lombok.Getter;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import ro.nico.tag.util.Direction;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkPos {
    @Getter
    private final int x, y, z;

    private ChunkPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private final static Pattern pattern = Pattern.compile("<x(-?[0-9]+),y(-?[0-9]+),z(-?[0-9]+)>");

    public ChunkPos getRelative(Direction direction) {
        return new ChunkPos(this.x + direction.getModX(), this.y + direction.getModY(), this.z + direction.getModZ());
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

    public static ChunkPos fromString(final String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int y = Integer.parseInt(matcher.group(2));
                int z = Integer.parseInt(matcher.group(3));
                return new ChunkPos(x, y, z);
            } else
                throw new IllegalArgumentException("ChunkUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("ChunkUUID couldn't parse from string.");
        }
    }

    public static ChunkPos fromLocation(Location location) {
        int chunkX = Math.floorDiv(location.getBlockX(), 16);
        int chunkY = Math.floorDiv(location.getBlockY(), 16);
        int chunkZ = Math.floorDiv(location.getBlockZ(), 16);
        return new ChunkPos(chunkX, chunkY, chunkZ);
    }

    @NotNull
    @Override
    public String toString() {
        return "<x" + this.x + ",y" + this.y + ",z" + this.z + ">";
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof ChunkPos other && this.x == other.x && this.y == other.y && this.z == other.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }
}
