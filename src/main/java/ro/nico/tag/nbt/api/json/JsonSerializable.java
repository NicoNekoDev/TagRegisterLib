package ro.nico.tag.nbt.api.json;

import com.google.gson.JsonObject;
import ro.nico.tag.nbt.api.Tag;
import ro.nico.tag.nbt.api.registry.TagTypeRegistry;

import java.io.IOException;

/**
 * Interface for JSON (de)serialization. Must be implemented if your tag will be JSON (de)serializable.
 *
 * @author dewy
 */
public interface JsonSerializable {
    /**
     * Serializes this tag into a GSON {@code JsonObject}.
     *
     * @param depth the current depth of the NBT data structure.
     * @return the serialized {@code JsonObject}.
     * @throws IOException if any I/O error occurs.
     */
    JsonObject toJson(int depth) throws IOException;

    /**
     * Deserializes this tag from a give {@code JsonObject}.
     *
     * @param json the {@code JsonObject} to be deserialized.
     * @param depth the current depth of the NBT data structure.
     * @return this (literally {@code return this;} after deserialization).
     * @throws IOException if any I/O error occurs.
     */
    Tag fromJson(JsonObject json, int depth) throws IOException;
}
