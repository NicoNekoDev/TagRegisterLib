package ro.nicuch.tag.thread;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.LinkedList;
import java.util.Queue;

public class TagRunnable implements Runnable {
    private final Queue<TagProcess> processes = new LinkedList<>();

    // ASYNC CALL
    @Override
    public void run() {
        while (!this.processes.isEmpty()) {
            this.processes.poll().run();
        }
    }

    // SYNC CALLS
    public void addToLoad(ChunkLoadEvent event) {
        this.processes.offer(new TagProcessLoad(event));
    }

    public void addToUnload(ChunkUnloadEvent event) {
        this.processes.offer(new TagProcessUnload(event));
    }
}
