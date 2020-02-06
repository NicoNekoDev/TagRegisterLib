package ro.nicuch.tag.thread;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TagRunnable implements Runnable {
    private final ConcurrentMap<TagProcessUUID, TagProcess> processes = new ConcurrentHashMap<>();

    // ASYNC CALL
    @Override
    public void run() {
        this.processes.forEach((key, value) -> {
            value.run();
            this.processes.remove(key);
        });
    }

    // SYNC CALLS
    public void addToLoad(ChunkLoadEvent event) {
        TagProcessLoad tagProcess = new TagProcessLoad(event);
        this.processes.putIfAbsent(tagProcess.getProcessId(), tagProcess);
    }

    public void addToUnload(ChunkUnloadEvent event) {
        TagProcessUnload tagProcess = new TagProcessUnload(event);
        this.processes.putIfAbsent(tagProcess.getProcessId(), tagProcess);
    }
}
