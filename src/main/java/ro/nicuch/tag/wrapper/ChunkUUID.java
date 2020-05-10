package ro.nicuch.tag.wrapper;

import org.bukkit.Chunk;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkUUID {
    private final int x;
    private final int z;

    private final static Pattern pattern = Pattern.compile("x([-]?[0-9]+),z([-]?[0-9]+)");

    public ChunkUUID(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    public ChunkUUID(final Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    public final int getX() {
        return this.x;
    }

    public final int getZ() {
        return this.z;
    }


    public static ChunkUUID fromString(String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int z = Integer.parseInt(matcher.group(2));
                return new ChunkUUID(x, z);
            } else
                throw new IllegalArgumentException("ChunkUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("ChunkUUID couldn't parse from string.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkUUID)) return false;
        ChunkUUID that = (ChunkUUID) obj;
        return this.x == that.x && this.z == that.z;
    }

    @Override
    public String toString() {
        return "x" + x + ",z" + z + "";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
