package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ro.nicuch.tag.thread.TagProcessRunnable;

import java.io.File;

public class TagPlugin extends JavaPlugin {
    private BukkitTask task;
    private File cacheDirectory;
    private TagProcessRunnable tagProcess;

    @Override
    public void onEnable() {
        this.tagProcess = new TagProcessRunnable();
        Bukkit.getPluginManager().registerEvents(new TagListener(this), this);
        this.autoUnload();
    }

    @Override
    public void onDisable() {
        this.task.cancel();
        this.tagProcess.shutdown();
        TagRegister.tryUnloading(); //last time
        TagRegister.saveAll();
    }

    public TagProcessRunnable getProcess() {
        return this.tagProcess;
    }

    private void autoUnload() {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, TagRegister::tryUnloading, 10 * 20L, 10 * 20L);
    }
}
