package ro.nicuch.tag.nbt.async;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

public interface AsyncInt2BooleanSortedMap {

    Int2BooleanSortedMap getMapDirectly();

    ReentrantReadWriteLock getLock();

    Future<Void> clear();

    Future<Boolean> put(int key, boolean value);

    Future<Boolean> replace(int key, boolean value);

    Future<Boolean> putIfAbsent(int key, boolean value);

    Future<Boolean> get(int key);

    Future<Boolean> containsKey(int key);

    Future<Boolean> containsValue(boolean value);

    Future<Boolean> remove(int key);

    Future<Boolean> remove(int key, boolean value);

    Future<Integer> size();

    Future<Boolean> compute(int key, BiFunction<? super Integer, ? super Boolean, ? extends Boolean> remappingFunction);

    Future<Boolean> computeIfAbsent(int key, Int2BooleanFunction mappingFunction);

    Future<Boolean> computeIfPresent(int key, BiFunction<? super Integer, ? super Boolean, ? extends Boolean> remappingFunction);

    ObjectSortedSet<Int2BooleanMap.Entry> int2BooleanEntrySet();

    Future<Void> fill(int position, int length, boolean value);
}
