package ro.nicuch.tag.nbt.async;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AsyncObject2ObjectHashMap<K, V> implements AsyncObject2ObjectMap<K, V> {
    private final Object2ObjectMap<K, V> map;
    private final ExecutorService executors;
    private final ReentrantReadWriteLock lock;
    private final ReadLock readLock;
    private final WriteLock writeLock;

    public AsyncObject2ObjectHashMap() {
        this(Hash.DEFAULT_INITIAL_SIZE, Hash.DEFAULT_LOAD_FACTOR, null, false);
    }

    public AsyncObject2ObjectHashMap(int capacity) {
        this(capacity, Hash.DEFAULT_LOAD_FACTOR, null, false);
    }

    public AsyncObject2ObjectHashMap(int capacity, float loadFactor) {
        this(capacity, loadFactor, null, false);
    }

    public AsyncObject2ObjectHashMap(int capacity, float loadFactor, ExecutorService executors, boolean fairLock) {
        this.map = new Object2ObjectOpenHashMap<>(capacity, loadFactor);
        this.executors = executors != null ? executors : Executors.newCachedThreadPool();
        this.lock = new ReentrantReadWriteLock(fairLock);
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
    }

    @Override
    public ReentrantReadWriteLock getLock() {
        return this.lock;
    }

    @Override
    public Future<Void> clear() {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                this.map.clear();
                return null;
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    @Override
    public Future<V> put(K key, V value) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.map.put(key, value);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    @Override
    public Future<V> get(K key) {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                return this.map.get(key);
            } finally {
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<Boolean> containsKey(K key) {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                return this.map.containsKey(key);
            } finally {
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<Boolean> containsValue(V value) {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                return this.map.containsValue(value);
            } finally {
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<V> remove(K key) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.map.remove(key);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    @Override
    public Future<Boolean> remove(K key, V value) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.map.remove(key, value);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    @Override
    public Future<Integer> size() {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                return this.map.size();
            } finally {
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<V> compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return this.executors.submit(() -> {
            // acquire both write and read locks
            this.writeLock.lock();
            this.readLock.lock();
            try {
                return this.map.compute(key, remappingFunction);
            } finally {
                this.writeLock.unlock();
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<V> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return this.executors.submit(() -> {
            // acquire both write and read locks
            this.writeLock.lock();
            this.readLock.lock();
            try {
                return this.map.computeIfAbsent(key, mappingFunction);
            } finally {
                this.writeLock.unlock();
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<V> computeIfPresent(K key, Function<? super K, ? extends V> mappingFunction) {
        return this.executors.submit(() -> {
            // acquire both write and read locks
            this.writeLock.lock();
            this.readLock.lock();
            try {
                return this.map.computeIfAbsent(key, mappingFunction);
            } finally {
                this.writeLock.unlock();
                this.readLock.unlock();
            }
        });
    }

    @Override
    public Future<Boolean> isEmpty() {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                return this.map.isEmpty();
            } finally {
                this.readLock.unlock();
            }
        });
    }

    @Override
    public ObjectSet<Object2ObjectMap.Entry<K, V>> object2ObjectEntrySet() {
        return this.map.object2ObjectEntrySet();
    }

    @Override
    public ObjectSet<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public ObjectCollection<V> values() {
        return this.map.values();
    }
}
