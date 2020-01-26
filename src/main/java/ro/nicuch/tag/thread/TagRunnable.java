package ro.nicuch.tag.thread;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TagRunnable implements Runnable {
    private final ConcurrentMap<UUID, TagProcess> processes = new ConcurrentHashMap<>();

    // ASYNC CALL
    @Override
    public void run() {
        for (TagProcess process : this.processes.values()) {
            process.run();
        }
        processes.clear();
    }

    // SYNC CALLS
    public void addToLoad(ChunkLoadEvent event) {
        this.processes.put(UUID.randomUUID(), new TagProcessLoad(event));
    }

    public void addToUnload(ChunkUnloadEvent event) {
        this.processes.put(UUID.randomUUID(), new TagProcessUnload(event));
    }
}
