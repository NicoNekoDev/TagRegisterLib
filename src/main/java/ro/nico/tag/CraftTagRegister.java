package ro.nico.tag;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.bukkit.Bukkit;
import ro.nico.tag.register.CraftWorldRegister;
import ro.nico.tag.wrapper.WorldId;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class CraftTagRegister {
    private static final Logger logger;
    public final static int MAXIMUM_DISTANCE = Bukkit.getViewDistance();
    private static final ConcurrentMap<WorldId, CraftWorldRegister> worlds = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, ExecutorService> chunkExecutors = new ConcurrentHashMap<>();
    private static final int chunkThreads = 8;
    private static final ExecutorService regionExecutor = Executors.newFixedThreadPool(8, new ThreadFactoryBuilder().setNameFormat("Region-Executor").build());

    static {
        logger = Logger.getLogger("TagRegisterLib");
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

    public static Collection<CraftWorldRegister> getWorlds() {
        return worlds.values();
    }

    public static Logger getLogger() {
        return logger;
    }
}
