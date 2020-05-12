package ro.nicuch.tag.wrapper;

import org.bukkit.Chunk;
import ro.nicuch.tag.register.RegionRegister;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionUUID {
    private final int x;
    private final int z;

    private final static Pattern pattern = Pattern.compile("<x([-]?[0-9]+),z([-]?[0-9]+)>");

    public RegionUUID(final RegionRegister register) {
        this(register.getX(), register.getZ());
    }

    public RegionUUID(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    public final int getX() {
        return this.x;
    }

    public final int getZ() {
        return this.z;
    }

    public static RegionUUID fromChunk(Chunk chunk) {
        return new RegionUUID(Math.floorDiv(chunk.getX(), 32), Math.floorDiv(chunk.getZ(), 32));
    }

    public static RegionUUID fromString(String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                int x = Integer.parseInt(matcher.group(1));
                int z = Integer.parseInt(matcher.group(2));
                return new RegionUUID(x, z);
            } else
                throw new IllegalArgumentException("RegionUUID couldn't parse from string.");
        } catch (IllegalStateException | NumberFormatException e) {
            throw new IllegalArgumentException("RegionUUID couldn't parse from string.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof RegionUUID))
            return false;
        RegionUUID regionUUID = (RegionUUID) obj;
        return x == regionUUID.getX() && z == regionUUID.getZ();
    }

    @Override
    public String toString() {
        return "<x" + x + ",z" + z + ">";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, this.toString());
    }
}
