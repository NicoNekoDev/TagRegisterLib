package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.register.WorldRegister;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TagRegister {
    private final static ConcurrentMap<String, WorldRegister> worlds = new ConcurrentHashMap<>();
    private final static Map<String, ReentrantLock> worldsLock = Collections.synchronizedMap(new WeakHashMap<>());
    private static boolean debug;

    private static ReentrantLock getWorldLock(String world) {
        if (worldsLock.containsKey(world))
            return worldsLock.get(world);
        ReentrantLock lock = new ReentrantLock();
        worldsLock.put(world, lock);
        return lock;
    }

    public static boolean isDebugging() {
        return debug;
    }

    public static void toggleDebug() {
        debug = !debug;
    }

    public static boolean isStored(Entity entity) {
        return getOrLoadWorld(entity.getWorld()).isEntityStored(entity);
    }

    public static boolean isStored(Block block) {
        return getOrLoadWorld(block.getWorld()).isBlockStored(block);
    }

    public static Optional<CompoundTag> getStored(Entity entity) {
        return getOrLoadWorld(entity.getWorld()).getStoredEntity(entity);
    }

    public static Optional<CompoundTag> getStored(Block block) {
        return getOrLoadWorld(block.getWorld()).getStoredBlock(block);
    }

    public static CompoundTag getStoredUnsafe(Entity entity) {
        return getOrLoadWorld(entity.getWorld()).getStoredEntityUnsafe(entity);
    }

    public static CompoundTag getStoredUnsafe(Block block) {
        return getOrLoadWorld(block.getWorld()).getStoredBlockUnsafe(block);
    }

    public static CompoundTag create(Entity entity) {
        return getOrLoadWorld(entity.getWorld()).createStoredEntity(entity);
    }

    public static CompoundTag getOrCreateEntity(Entity entity) {
        return getOrLoadWorld(entity.getWorld()).getOrCreateEntity(entity);
    }

    public static CompoundTag create(Block block) {
        return getOrLoadWorld(block.getWorld()).createStoredBlock(block);
    }

    public static CompoundTag getOrCreateBlock(Block block) {
        return getOrLoadWorld(block.getWorld()).getOrCreateBlock(block);
    }

    public static boolean isWorldLoaded(World world) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);
        lock.lock();
        try {
            return worlds.containsKey(worldName);
        } finally {
            lock.unlock();
        }
    }

    public static WorldRegister loadWorld(World world) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);
        lock.lock();
        try {
            WorldRegister wr = new WorldRegister(world);
            worlds.put(worldName, wr);
            return wr;
        } finally {
            lock.unlock();
        }
    }

    public static WorldRegister unloadWorld(World world) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);
        lock.lock();
        try {
            return worlds.remove(worldName);
        } finally {
            lock.unlock();
        }
    }

    public static Optional<WorldRegister> getWorld(World world) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);
        lock.lock();
        try {
            return Optional.ofNullable(worlds.get(worldName));
        } finally {
            lock.unlock();
        }
    }

    public static WorldRegister getWorldUnsafe(World world) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);
        lock.lock();
        try {
            return worlds.get(worldName);
        } finally {
            lock.unlock();
        }
    }

    public static WorldRegister getOrLoadWorld(World world) {
        String worldName = world.getName();
        ReentrantLock lock = getWorldLock(worldName);
        lock.lock();
        try {
            if (worlds.containsKey(world.getName()))
                return worlds.get(world.getName());
            WorldRegister wr = new WorldRegister(world);
            worlds.put(world.getName(), wr);
            return wr;
        } finally {
            lock.unlock();
        }
    }

    public static void tryUnloading() {
        for (Map.Entry<String, WorldRegister> entry : worlds.entrySet()) {
            ReentrantLock lock = getWorldLock(entry.getKey());
            lock.lock();
            try {
                entry.getValue().tryUnloading();
            } finally {
                lock.unlock();
            }
        }
        /*for (WorldRegister world : worlds.values()) {
            world.tryUnloading();
        }*/
    }

    public static void saveAll() {
        for (Map.Entry<String, WorldRegister> entry : worlds.entrySet()) {
            ReentrantLock lock = getWorldLock(entry.getKey());
            lock.lock();
            try {
                entry.getValue().saveRegions();
            } finally {
                lock.unlock();
            }
        }
        /*for (WorldRegister world : worlds.values())
            world.saveRegions();*/
    }

    public static Logger getLogger() {
        return Bukkit.getLogger();
    }

    public static TagPlugin getPlugin() {
        return (TagPlugin) Bukkit.getPluginManager().getPlugin("TagRegisterLib");
    }
}
