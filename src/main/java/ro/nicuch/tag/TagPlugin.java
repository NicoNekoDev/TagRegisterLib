package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class TagPlugin extends JavaPlugin {
    private BukkitTask task;
    private File cacheDirectory;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);
        this.autoUnload();
    }

    @Override
    public void onDisable() {
        this.task.cancel();
        TagRegister.tryUnloading(); //last time
        TagRegister.saveAll();
    }

    private void autoUnload() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, TagRegister::tryUnloading, 10 * 20L, 10 * 20L);
    }
}
