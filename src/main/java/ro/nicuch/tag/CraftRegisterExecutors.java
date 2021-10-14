package ro.nicuch.tag;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CraftRegisterExecutors {
    private static final ConcurrentMap<Integer, ExecutorService> chunkExecutors = new ConcurrentHashMap<>();
    private static final int chunkThreads = 8;
    private static final ExecutorService regionExecutor = Executors.newSingleThreadExecutor();

    static {
        for (int n = 0; n < chunkThreads; n++)
            chunkExecutors.put(n, Executors.newSingleThreadExecutor());
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
