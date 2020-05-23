package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
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
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldUnloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions();
            TagRegister.unloadWorld(event.getWorld());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().isOp())
            return;
        if (event.getMessage().equalsIgnoreCase("/save-all"))
            Bukkit.getScheduler().runTaskAsynchronously(plugin, TagRegister::saveAll);
    }
}
