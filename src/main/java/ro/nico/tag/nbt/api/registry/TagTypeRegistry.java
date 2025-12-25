package ro.nico.tag.nbt.api.registry;

import org.jetbrains.annotations.NotNull;
import ro.nico.tag.nbt.api.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A registry mapping {@code byte} tag type IDs to tag type classes. Used to register custom-made {@link Tag} types.
 *
 * @author dewy
 */
public class TagTypeRegistry {
    private static final Map<Byte, Supplier<Tag>> idRegistry = new HashMap<>();
    private static final Map<String, Byte> nameToId = new HashMap<>();
    private static final Map<Byte, String> idToName = new HashMap<>();

    /**
     * Register a custom-made tag type with a unique {@code byte} ID. IDs 0-12 (inclusive) are reserved and may not be used.
     *
     * @param id      the tag type's unique ID used in reading and writing.
     * @param name    the tag type's unique name used in reading and writing.
     * @param factory the tag factory.
     * @throws TagTypeRegistryException if the ID provided is either registered already or is a reserved ID (0-12 inclusive).
     */
    public static void registerTagType(byte id, @NotNull String name, @NotNull Supplier<Tag> factory) throws TagTypeRegistryException {
        if (id == 0) {
            throw new TagTypeRegistryException("Cannot register NBT tag type " + name + " with ID " + id + ", as that ID is reserved.");
        }

        if (idRegistry.containsKey(id)) {
            throw new TagTypeRegistryException("Cannot register NBT tag type " + name + " with ID " + id + ", as that ID is already in use by the tag type " + idToName.get(id));
        }

        if (idRegistry.containsValue(factory)) {
            byte existing = 0;
            for (Map.Entry<Byte, Supplier<Tag>> entry : idRegistry.entrySet()) {
                if (entry.getValue().equals(factory)) {
                    existing = entry.getKey();
                }
            }

            throw new TagTypeRegistryException("NBT tag type " + name + " already registered under ID " + existing);
        }

        idRegistry.put(id, factory);
        nameToId.put(name, id);
        idToName.put(id, name);
    }

    /**
     * Deregister a custom-made tag type with a provided tag type ID.
     *
     * @param id the ID of the tag type to deregister.
     * @return if the tag type was deregistered successfully.
     */
    public static boolean unregisterTagType(byte id) {
        if (id >= 0 && id <= 12) {
            return false;
        }

        String name = idToName.get(id);
        if (name == null) return false;

        return idRegistry.remove(id) != null && nameToId.remove(name) != null && idToName.remove(id) != null;
    }

    /**
     * Deregister a custom-made tag type with a provided tag name.
     *
     * @param name the name of the tag type to deregister.
     * @return if the tag type was deregistered successfully.
     */
    public static boolean unregisterTagType(@NotNull String name) {
        byte id = nameToId.get(name);

        return unregisterTagType(id);
    }

    /**
     * Deregister a custom-made tag type with a provided tag type ID and class value.
     *
     * @param id      the ID of the tag type to deregister.
     * @param factory the factory of the tag type to deregister.
     * @return if the tag type was deregistered successfully.
     */
    public static boolean unregisterTagType(byte id, Supplier<Tag> factory) {
        String name = idToName.get(id);
        if (name == null) return false;

        return idRegistry.remove(id, factory) && nameToId.remove(name, id) && idToName.remove(id, name);
    }

    /**
     * Deregister a custom-made tag type with a provided tag type ID and class value.
     *
     * @param name    the name of the tag type to deregister.
     * @param factory the factory of the tag type to deregister.
     * @return if the tag type was deregistered successfully.
     */
    public static boolean unregisterTagType(@NotNull String name, Supplier<Tag> factory) {
        Byte id = nameToId.get(name);
        if (id == null) return false;

        return unregisterTagType(id, factory);
    }

    /**
     * Returns a tag type factory from the registry from a provided {@code byte} ID.
     *
     * @param id the ID of the tag type to retrieve.
     * @return a tag type factory from the registry from a provided {@code byte} ID.
     */
    public static Supplier<Tag> getFactoryFromId(byte id) {
        return idRegistry.get(id);
    }

    /**
     * Returns a tag type factory from the registry from a provided {@code name}.
     *
     * @param name the name of the tag type to retrieve.
     * @return a tag type factory from the registry from a provided {@code name}.
     */
    public static Supplier<Tag> getFactoryFromId(@NotNull String name) {
        Byte id = nameToId.get(name);
        if (id == null) return null;

        return getFactoryFromId(id);
    }
}
