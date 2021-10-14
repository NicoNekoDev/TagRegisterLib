package ro.nicuch.tag.async;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import it.unimi.dsi.fastutil.ints.Int2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.BiFunction;

public class AsyncInt2BooleanSortedHashMap implements AsyncInt2BooleanSortedMap {
    private final Int2BooleanSortedMap map;
    private final ExecutorService executors;
    private final ReentrantReadWriteLock lock;
    private final ReadLock readLock;
    private final WriteLock writeLock;

    public AsyncInt2BooleanSortedHashMap() {
        this(Hash.DEFAULT_INITIAL_SIZE, Hash.DEFAULT_LOAD_FACTOR, null, false);
    }

    public AsyncInt2BooleanSortedHashMap(int capacity) {
        this(capacity, Hash.DEFAULT_LOAD_FACTOR, null, false);
    }

    public AsyncInt2BooleanSortedHashMap(int capacity, float loadFactor) {
        this(capacity, loadFactor, null, false);
    }

    public AsyncInt2BooleanSortedHashMap(int capacity, float loadFactor, ExecutorService executors, boolean fairLock) {
        this.map = new Int2BooleanLinkedOpenHashMap(capacity, loadFactor);
        this.executors = executors != null ? executors : Executors.newCachedThreadPool();
        this.lock = new ReentrantReadWriteLock(fairLock);
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
    }

    @Override
    public Int2BooleanSortedMap getMapDirectly() {
        return this.map;
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
    public Future<Boolean> put(int key, boolean value) {
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
    public Future<Boolean> replace(int key, boolean value) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.map.replace(key, value);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    @Override
    public Future<Boolean> putIfAbsent(int key, boolean value) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.map.putIfAbsent(key, value);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    @Override
    public Future<Boolean> get(int key) {
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
    public Future<Boolean> containsKey(int key) {
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
    public Future<Boolean> containsValue(boolean value) {
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
    public Future<Boolean> remove(int key) {
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
    public Future<Boolean> remove(int key, boolean value) {
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
    public Future<Boolean> compute(int key, BiFunction<? super Integer, ? super Boolean, ? extends Boolean> remappingFunction) {
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
    public Future<Boolean> computeIfAbsent(int key, Int2BooleanFunction mappingFunction) {
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
    public Future<Boolean> computeIfPresent(int key, BiFunction<? super Integer, ? super Boolean, ? extends Boolean> remappingFunction) {
        return this.executors.submit(() -> {
            // acquire both write and read locks
            this.writeLock.lock();
            this.readLock.lock();
            try {
                return this.map.computeIfPresent(key, remappingFunction);
            } finally {
                this.writeLock.unlock();
                this.readLock.unlock();
            }
        });
    }

    @Override
    public ObjectSortedSet<Int2BooleanMap.Entry> int2BooleanEntrySet() {
        return this.map.int2BooleanEntrySet();
    }

    @Override
    public Future<Void> fill(int position, int length, boolean value) {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                int end = position + length;
                for (int n = position; n < end; n++)
                    this.map.put(n, value);
                return null;
            } finally {
                this.readLock.unlock();
            }
        });
    }
}
