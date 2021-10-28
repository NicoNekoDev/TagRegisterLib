package ro.nicuch.tag.wrapper;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WorldId(String name) {

    private final static Pattern pattern = Pattern.compile("<world-([a-zA-Z0-9_-]+)>");
    private final static Pattern validator = Pattern.compile("^([a-zA-Z0-9_-]+)$");

    public WorldId {
        Matcher matcher = validator.matcher(name);
        if (!matcher.find())
            throw new IllegalArgumentException("The world name is not a valid!");
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "<world-" + this.name + ">";
    }

    public static WorldId fromString(final String id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldId that = (WorldId) o;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
}
