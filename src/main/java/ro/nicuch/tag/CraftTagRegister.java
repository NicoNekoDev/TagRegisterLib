package ro.nicuch.tag;

import ro.nicuch.tag.register.CraftWorldRegister;
import ro.nicuch.tag.wrapper.WorldUUID;

import java.util.concurrent.*;

public class CraftTagRegister {
    public final static int MAXIMUM_DISTANCE = 10;
    private static final ConcurrentMap<WorldUUID, CraftWorldRegister> worlds = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, ExecutorService> chunkExecutors = new ConcurrentHashMap<>();
    private static final int chunkThreads = 8;
    private static final ExecutorService regionExecutor = Executors.newSingleThreadExecutor();

    static {
        for (int n = 0; n < chunkThreads; n++)
            chunkExecutors.put(n, Executors.newSingleThreadExecutor());
    }

    public static CraftWorldRegister getWorldRegister(WorldUUID worldId) {
        return worlds.computeIfAbsent(worldId, CraftWorldRegister::new);
    }

    public static int getChunkThreads() {
        return chunkThreads;
    }

    public static ExecutorService getChunkExecutor(int thread) {
        return chunkExecutors.get(thread);
    }

    public static ExecutorService getRegionExecutor() {
        return regionExecutor;
    }
}
