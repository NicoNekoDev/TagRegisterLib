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
        for (ConcurrentMap.Entry<TagProcessUUID, TagProcess> entry : this.processes.entrySet()) {
            entry.getValue().run();
            this.processes.remove(entry.getKey());
        }
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
