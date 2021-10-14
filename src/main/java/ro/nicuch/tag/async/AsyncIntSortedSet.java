package ro.nicuch.tag.async;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AsyncIntSortedSet {
    private final IntAVLTreeSet set;
    private final ExecutorService executors;
    private final ReentrantReadWriteLock lock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;

    public AsyncIntSortedSet() {
        this(null, false);
    }

    public AsyncIntSortedSet(ExecutorService executors, boolean fairLock) {
        this.set = new IntAVLTreeSet(Comparator.naturalOrder());
        this.executors = executors != null ? executors : Executors.newCachedThreadPool();
        this.lock = new ReentrantReadWriteLock(fairLock);
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
    }

    public ReentrantReadWriteLock getLock() {
        return this.lock;
    }

    public Future<Void> fill(int position, int length) {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                int end = position + length;
                for (int n = position; n < end; n++)
                    this.set.add(n);
                return null;
            } finally {
                this.readLock.unlock();
            }
        });
    }

    public Future<Boolean> remove(int key) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.set.remove(key);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    public Future<Boolean> add(int key) {
        return this.executors.submit(() -> {
            this.writeLock.lock();
            try {
                return this.set.add(key);
            } finally {
                this.writeLock.unlock();
            }
        });
    }

    public IntAVLTreeSet getSetDirectly() {
        return this.set;
    }

    public Future<Boolean> isEmpty() {
        return this.executors.submit(() -> {
            this.readLock.lock();
            try {
                return this.set.isEmpty();
            } finally {
                this.readLock.unlock();
            }
        });
    }
}
