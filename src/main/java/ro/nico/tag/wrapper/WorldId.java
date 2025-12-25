package ro.nico.tag.wrapper;

import lombok.Getter;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldId {

    private final static Pattern pattern = Pattern.compile("<world-([a-zA-Z0-9_-]+)>");
    private final static Pattern validator = Pattern.compile("^([a-zA-Z0-9_-]+)$");

    @Getter
    private final String name;

    private WorldId(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String toString() {
        return "<world-" + this.name + ">";
    }

    public static @NotNull WorldId fromString(final String id) {
        try {
            Matcher matcher = pattern.matcher(id);
            if (matcher.find()) {
                String name = matcher.group(1);
                return new WorldId(name);
            } else
                throw new IllegalArgumentException("WorldUUID couldn't parse from string.");
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("WorldUUID couldn't parse from string.");
        }
    }

    public static @NotNull WorldId fromWorld(String name) {
        Matcher matcher = validator.matcher(name);
        if (!matcher.find())
            throw new IllegalArgumentException("The world name is not a valid!");
        return new WorldId(name);
    }

    public static @NotNull WorldId fromWorld(World world) {
        return new WorldId(world.getName());
    }

    @Override
    public boolean equals(Object that) {
        return this == that || (that instanceof WorldId other && this.name.equals(other.name));
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
