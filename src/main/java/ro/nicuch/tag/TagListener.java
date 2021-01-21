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
import ro.nicuch.tag.register.WorldRegister;
import ro.nicuch.tag.thread.TagProcessRunnable;

import java.util.Optional;

public class TagListener implements Listener {
    private final TagPlugin plugin;
    private final TagProcessRunnable tagProcess;

    public TagListener(TagPlugin plugin) {
        this.tagProcess = (this.plugin = plugin).getProcess();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void event(ChunkLoadEvent event) {
        this.tagProcess.addToLoad(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void event(ChunkUnloadEvent event) {
        this.tagProcess.addToUnload(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void event(WorldSaveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> TagRegister.getOrLoadWorld(event.getWorld()).saveRegions());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void event(WorldUnloadEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<WorldRegister> optionalWorldRegister = TagRegister.getWorld(event.getWorld());
            if (!optionalWorldRegister.isPresent())
                return;
            optionalWorldRegister.get().saveRegions();
            TagRegister.unloadWorld(event.getWorld());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void event(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().isOp())
            return;
        if (event.getMessage().equalsIgnoreCase("/save-all"))
            Bukkit.getScheduler().runTaskAsynchronously(plugin, TagRegister::saveAll);
    }
}
