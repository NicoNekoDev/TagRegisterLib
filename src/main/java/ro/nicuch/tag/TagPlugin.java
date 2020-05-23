package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ro.nicuch.tag.fallback.CoruptedDataCommands;
import ro.nicuch.tag.fallback.CoruptedDataListener;

import java.io.File;

public class TagPlugin extends JavaPlugin {
    private BukkitTask task;
    private File cacheDirectory;

    @Override
    public void onEnable() {
        this.cacheDirectory = new File(this.getDataFolder() + File.separator + "cache" + File.separator);
        if (this.cacheDirectory.exists())
            this.cacheDirectory.delete();
        this.cacheDirectory.mkdirs();
        CoruptedDataCommands commands = new CoruptedDataCommands();
        this.getCommand("trl_backup").setExecutor(commands);
        this.getCommand("trl_overwrite").setExecutor(commands);
        this.getCommand("trl_reset").setExecutor(commands);
        this.getCommand("trl_debug").setExecutor(commands);
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CoruptedDataListener(), this);
        this.autoUnload();
    }

    @Override
    public void onDisable() {
        this.task.cancel();
        TagRegister.tryUnloading(); //last time
        TagRegister.saveAll();
        if (this.cacheDirectory.exists())
            this.cacheDirectory.delete();
    }

    private void autoUnload() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, TagRegister::tryUnloading, 10 * 20L, 10 * 20L);
    }
}
