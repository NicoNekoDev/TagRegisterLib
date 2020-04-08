package ro.nicuch.tag.thread;

import com.mfk.lockfree.queue.LockFreeQueue;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class TagProcessRunnable implements Runnable {
    private static final LockFreeQueue<TagRunnable> processes = LockFreeQueue.newQueue(32);

    // ASYNC CALL
    @Override
    public void run() {
        while (processes.size() > 0) {
            processes.poll().ifPresent(TagRunnable::run);
        }
    }

    // SYNC CALLS
    public static void addToLoad(ChunkLoadEvent event) {
        processes.add(new TagProcessLoad(event));
    }

    public static void addToUnload(ChunkUnloadEvent event) {
        processes.add(new TagProcessUnload(event));
    }
}
