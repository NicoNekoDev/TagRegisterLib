package ro.nicuch.tag.thread;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TagProcessRunnable implements Runnable {
    private static final ConcurrentLinkedQueue<TagRunnable> processes = new ConcurrentLinkedQueue<>();

    // ASYNC CALL
    @Override
    public void run() {
        while (!processes.isEmpty()) {
            processes.poll().run();
        }
    }

    // SYNC CALLS
    public static void addToLoad(ChunkLoadEvent event) {
        processes.offer(new TagProcessLoad(event));
    }

    public static void addToUnload(ChunkUnloadEvent event) {
        processes.offer(new TagProcessUnload(event));
    }
}
