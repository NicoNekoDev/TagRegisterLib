package ro.nico.tag.nbt.api.snbt;

/**
 * Interface for SNBT serialization. Must be implemented if your tag will be SNBT serializable. Reading is not yet supported.
 *
 * @author dewy
 */
public interface SnbtSerializable {
    String toSnbt(int depth, SnbtConfig config);
}
