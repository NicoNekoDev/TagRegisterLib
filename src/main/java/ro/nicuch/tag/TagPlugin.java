package ro.nicuch.tag;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nicuch.tag.register.CraftWorldRegister;
import ro.nicuch.tag.util.TicketType;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.WorldUUID;

import java.util.concurrent.TimeUnit;

public class TagPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void shift(PlayerToggleSneakEvent event) {
        if (event.isSneaking())
            return;
        Player player = event.getPlayer();
        WorldUUID worldUUID = WorldUUID.fromWorld(player.getWorld());
        CraftWorldRegister worldRegister = CraftTagRegister.getWorldRegister(worldUUID);
        ChunkUUID chunkId = ChunkUUID.fromLocation(player.getLocation().getBlock().getRelative(BlockFace.DOWN, 2));
        switch (player.getInventory().getItemInMainHand().getType()) {
            case DIAMOND -> worldRegister.setChunkTicket(chunkId, TicketType.OTHER, 2, 10, TimeUnit.SECONDS);
            case IRON_INGOT -> worldRegister.removeChunkTicket(chunkId);
        }
    }
}
