package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import ro.nicuch.tag.thread.TagProcessRunnable;

public class TagListener implements Listener {
    private final TagPlugin plugin;

    public TagListener(TagPlugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously((this.plugin = plugin), new TagProcessRunnable(), 1L, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(ChunkLoadEvent event) {
        TagProcessRunnable.addToLoad(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(ChunkUnloadEvent event) {
        TagProcessRunnable.addToUnload(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldSaveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions());
    }
}
