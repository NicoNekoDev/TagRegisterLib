package ro.nicuch.tag.util;

import io.github.NicoNekoDev.SimpleTuples.func.TripletConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class SelfExpiringMap<K, V> {
    private final ScheduledExecutorService executor;
    private final ConcurrentMap<K, V> internalMap;
    private final ConcurrentMap<K, ScheduledFuture<?>> timerTasks;
    private final TripletConsumer<K, V, EvictionType> removeCallback;

    public SelfExpiringMap() {
        this((key, value, type) -> {
        }, 16, Executors.newSingleThreadScheduledExecutor());
    }

    public SelfExpiringMap(TripletConsumer<K, V, EvictionType> removeCallback, ScheduledExecutorService executor) {
        this(removeCallback, 16, executor);
    }

    public SelfExpiringMap(TripletConsumer<K, V, EvictionType> removeCallback, int capacity, ScheduledExecutorService executor) {
        this(removeCallback, capacity, 0.75f, executor);
    }

    public SelfExpiringMap(TripletConsumer<K, V, EvictionType> removeCallback, int capacity, float loadFactor, ScheduledExecutorService executor) {
        this.internalMap = new ConcurrentHashMap<>(capacity, loadFactor);
        this.timerTasks = new ConcurrentHashMap<>(capacity, loadFactor);
        this.removeCallback = removeCallback;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void renewKey(K key, long time, TimeUnit timeUnit) {
        this.cancelFuture(key);
        ScheduledFuture<?> future = this.executor.schedule(() -> {
            V value = SelfExpiringMap.this.internalMap.get(key);
            this.removeCallback.accept(key, value, EvictionType.KEY_RENEW);
            SelfExpiringMap.this.internalMap.remove(key);
            SelfExpiringMap.this.timerTasks.remove(key);
        }, time, timeUnit);
        this.timerTasks.put(key, future);
    }

    public void expireKey(K key) {
        this.cancelFuture(key);
        this.executor.submit(() -> {
            V value = SelfExpiringMap.this.internalMap.get(key);
            this.removeCallback.accept(key, value, EvictionType.KEY_EXPIRE);
            SelfExpiringMap.this.internalMap.remove(key);
            SelfExpiringMap.this.timerTasks.remove(key);
        });
    }

    private void cancelFuture(K key) {
        ScheduledFuture<?> future = this.timerTasks.get(key);
        if (future != null)
            future.cancel(true);
    }

    public V put(K key, V value, long time, TimeUnit timeUnit) {
        V oldValue = this.internalMap.put(key, value);
        ScheduledFuture<?> future = this.executor.schedule(() -> {
            this.removeCallback.accept(key, value, EvictionType.SELF_EXPIRE);
            SelfExpiringMap.this.internalMap.remove(key);
            SelfExpiringMap.this.timerTasks.remove(key);
        }, time, timeUnit);
        this.timerTasks.put(key, future);
        return oldValue;
    }

    public int size() {
        return this.internalMap.size();
    }

    public boolean isEmpty() {
        return this.internalMap.isEmpty();
    }

    public boolean containsKey(K key) {
        return this.internalMap.containsKey(key);
    }

    public boolean containsValue(V value) {
        return this.internalMap.containsValue(value);
    }

    public V get(K key) {
        return this.internalMap.get(key);
    }

    public V remove(K key) {
        V oldValue = this.internalMap.get(key);
        this.cancelFuture(key);
        if (oldValue != null)
            this.removeCallback.accept(key, oldValue, EvictionType.EXPLICIT);
        this.internalMap.remove(key);
        return oldValue;
    }

    public void clear() {
        this.internalMap.clear();
        this.executor.shutdownNow();
        this.timerTasks.clear();
    }

    @NotNull
    public Set<K> keySet() {
        return this.internalMap.keySet();
    }

    @NotNull
    public Collection<V> values() {
        return this.internalMap.values();
    }

    @NotNull
    public Set<Map.Entry<K, V>> entrySet() {
        return this.internalMap.entrySet();
    }

    public enum EvictionType {
        SELF_EXPIRE, KEY_EXPIRE, KEY_RENEW, EXPLICIT
    }
}
