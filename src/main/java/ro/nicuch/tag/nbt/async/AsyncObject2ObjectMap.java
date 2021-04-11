package ro.nicuch.tag.nbt.async;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface AsyncObject2ObjectMap<K, V> {

    ReentrantReadWriteLock getLock();

    Future<Void> clear();

    Future<V> put(K key, V value);

    Future<V> get(K key);

    Future<Boolean> containsKey(K key);

    Future<Boolean> containsValue(V value);

    Future<V> remove(K key);

    Future<Boolean> remove(K key, V value);

    Future<Integer> size();

    Future<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction);

    Future<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction);

    Future<V> computeIfPresent(K key, Function<? super K, ? extends V> mappingFunction);

    Future<Boolean> isEmpty();

    ObjectSet<K> keySet();

    ObjectSet<Object2ObjectMap.Entry<K, V>> object2ObjectEntrySet();

    ObjectCollection<V> values();
}
