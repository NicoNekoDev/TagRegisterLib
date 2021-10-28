package ro.nicuch.tag;

import com.github.steveice10.packetlib.Session;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import ro.nicuch.tag.register.CraftWorldRegister;
import ro.nicuch.tag.wrapper.WorldId;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CraftTagRegister {
    public final static int MAXIMUM_DISTANCE = 10;
    private static final ConcurrentMap<WorldId, CraftWorldRegister> worlds = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, ExecutorService> chunkExecutors = new ConcurrentHashMap<>();
    private static final int chunkThreads = 8;
    private static final ExecutorService regionExecutor = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setNameFormat("Region-Executor").build());

    static {
        for (int n = 0; n < chunkThreads; n++)
            chunkExecutors.put(n, Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Chunk-Thread-" + n).build()));
    }

    public static CraftWorldRegister getWorldRegister(WorldId worldId) {
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

    public void sendChunks(Session session) {

    }
}
