package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ro.nicuch.tag.fallback.CoruptedDataCommands;
import ro.nicuch.tag.fallback.CoruptedDataListener;

import java.io.File;

public class TagPlugin extends JavaPlugin {
    private BukkitTask task;
    private File cacheFile;

    @Override
    public void onEnable() {
        cacheFile = new File(this.getDataFolder() + File.separator + "data.cache");
        CoruptedDataCommands commands = new CoruptedDataCommands();
        this.getCommand("trl_backup").setExecutor(commands);
        this.getCommand("trl_overwrite").setExecutor(commands);
        this.getCommand("trl_reset").setExecutor(commands);
        this.getCommand("trl_debug").setExecutor(commands);
        Bukkit.getPluginManager().registerEvents(new TagListener(), this);
        Bukkit.getPluginManager().registerEvents(new CoruptedDataListener(), this);
        this.autoUnload();
    }

    @Override
    public void onDisable() {
        this.task.cancel();
        TagRegister.tryUnloading(); //last time
        TagRegister.saveAll();
    }

    public File getCacheFile() {
        return this.cacheFile;
    }

    private void autoUnload() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, TagRegister::tryUnloading, 10 * 20L, 10 * 20L);
    }
}
