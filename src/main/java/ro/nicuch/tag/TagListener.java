package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class TagListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldSaveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(TagRegister.getPlugin(), () ->
                TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldUnloadEvent event) {
        TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions();
        TagRegister.unloadWorld(event.getWorld());
    }
}
