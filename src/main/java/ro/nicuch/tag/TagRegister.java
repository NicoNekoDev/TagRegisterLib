package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.register.PlayerRegister;
import ro.nicuch.tag.register.WorldRegister;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class TagRegister {

    private static boolean debug;

    public static boolean isDebugging() {
        return debug;
    }

    public static void toggleDebug() {
        debug = !debug;
    }

    private final static ConcurrentMap<String, WorldRegister> worlds = new ConcurrentHashMap<>();
    private final static ConcurrentMap<UUID, PlayerRegister> players = new ConcurrentHashMap<>();

    public static Optional<PlayerRegister> getPlayerRegister(UUID uuid) {
        if (players.containsKey(uuid))
            return Optional.ofNullable(players.get(uuid));
        PlayerRegister playerRegister = new PlayerRegister(uuid);
        players.put(uuid, playerRegister);
        return Optional.of(playerRegister);
    }

    public static Optional<CompoundTag> getPlayerTag(UUID uuid) {
        return Optional.ofNullable(players.get(uuid).getPlayerTag());
    }

    public static boolean isPlayerStored(UUID uuid) {
        return PlayerRegister.isPlayerStored(uuid);
    }

    public static Optional<PlayerRegister> getPlayerRegister(OfflinePlayer player) {
        return getPlayerRegister(player.getUniqueId());
    }

    public static boolean isStored(Entity entity) {
        if (entity instanceof Player)
            return isPlayerStored(entity.getUniqueId());
        return getWorld(entity.getWorld()).orElseGet(() -> loadWorld(entity.getWorld())).isEntityStored(entity);
    }

    public static boolean isStored(Block block) {
        return getWorld(block.getWorld()).orElseGet(() -> loadWorld(block.getWorld())).isBlockStored(block);
    }

    public static Optional<CompoundTag> getStored(Entity entity) {
        if (entity instanceof Player)
            return getPlayerTag(entity.getUniqueId());
        return getWorld(entity.getWorld()).orElseGet(() -> loadWorld(entity.getWorld())).getStoredEntity(entity);
    }

    public static Optional<CompoundTag> getStored(Block block) {
        return getWorld(block.getWorld()).orElseGet(() -> loadWorld(block.getWorld())).getStoredBlock(block);
    }

    public static CompoundTag create(Entity entity) {
        if (entity instanceof Player)
            return getPlayerRegister(entity.getUniqueId()).orElseGet(() -> players.put(entity.getUniqueId(), new PlayerRegister(entity.getUniqueId()))).getPlayerTag(); //Might not be null, ever
        return getWorld(entity.getWorld()).orElseGet(() -> loadWorld(entity.getWorld())).createStoredEntity(entity);
    }

    public static CompoundTag create(Block block) {
        return getWorld(block.getWorld()).orElseGet(() -> loadWorld(block.getWorld())).createStoredBlock(block);
    }

    public static boolean isWorldLoaded(World world) {
        return worlds.containsKey(world.getName());
    }

    public static WorldRegister loadWorld(World world) {
        WorldRegister wr = new WorldRegister(world);
        worlds.put(world.getName(), wr);
        return wr;
    }

    public static WorldRegister unloadWorld(World world) {
        return worlds.remove(world.getName());
    }

    public static Optional<WorldRegister> getWorld(World world) {
        return Optional.ofNullable(worlds.get(world.getName()));
    }

    public static void tryUnloading() {
        for (WorldRegister world : worlds.values())
            world.tryUnloading();
        for (UUID uuid : players.keySet()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (offlinePlayer.isOnline())
                continue;
            PlayerRegister playerRegister = players.get(uuid);
            players.remove(uuid);
            playerRegister.writePlayerFile();
        }
    }

    public static void saveAll() {
        for (WorldRegister world : worlds.values())
            world.saveRegions();
        for (PlayerRegister playerRegister : players.values())
            playerRegister.writePlayerFile();
    }

    public static Logger getLogger() {
        return Bukkit.getLogger();
    }

    public static TagPlugin getPlugin() {
        return (TagPlugin) Bukkit.getPluginManager().getPlugin("TagRegisterLib");
    }
}
