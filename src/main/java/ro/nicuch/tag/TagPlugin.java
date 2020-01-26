package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ro.nicuch.tag.fallback.CoruptedDataCommands;
import ro.nicuch.tag.fallback.CoruptedDataListener;

public class TagPlugin extends JavaPlugin {
    private BukkitTask task;

    @Override
    public void onEnable() {
        CoruptedDataCommands commands = new CoruptedDataCommands();
        this.getCommand("trl_backup").setExecutor(commands);
        this.getCommand("trl_overwrite").setExecutor(commands);
        this.getCommand("trl_reset").setExecutor(commands);
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CoruptedDataListener(), this);
        this.autoUnload();
    }

    @Override
    public void onDisable() {
        this.task.cancel();
        TagRegister.saveAll();
    }

    private void autoUnload() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, TagRegister::tryUnloading, 60 * 20L, 10 * 20L);
    }
}
