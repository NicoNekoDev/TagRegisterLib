package ro.nicuch.tag.async;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface AsyncInt2ObjectMap<V> {

    ReentrantReadWriteLock getLock();

    Future<Void> clear();

    Future<V> put(int key, V value);

    Future<V> replace(int key, V value);

    Future<V> get(int key);

    Future<Boolean> containsKey(int key);

    Future<Boolean> containsValue(V value);

    Future<V> remove(int key);

    Future<Boolean> remove(int key, V value);

    Future<Integer> size();

    Future<V> compute(int key, BiFunction<? super Integer, ? super V, ? extends V> remappingFunction);

    Future<V> computeIfAbsent(int key, Int2ObjectFunction<V> mappingFunction);

    Future<V> computeIfPresent(int key, Int2ObjectFunction<V> remappingFunction);

    ObjectSet<Int2ObjectMap.Entry<V>> int2ObjectEntrySet();

    Future<Void> fill(int position, int length, V value);

    Future<Void> fill(int position, int length, Function<? super Integer, ? extends V> mappingFunction);
}
