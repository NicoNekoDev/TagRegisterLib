package ro.nicuch.tag.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ro.nicuch.tag.nbt.CompoundTag;

public class TagLoadEvent extends Event {
    private final static HandlerList handlers = new HandlerList();
    private final CompoundTag tag;

    public TagLoadEvent(CompoundTag tag) {
        this.tag = tag;
    }

    public CompoundTag getCompoundTag() {
        return this.tag;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
