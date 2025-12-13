package ro.nico.tag.nbt;

import java.util.List;

/**
 * An indexed collection tag.
 */
public interface IndexedCollectionTag<T extends Tag> extends CollectionTag, List<T> {
}
