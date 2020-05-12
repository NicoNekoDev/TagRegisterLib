package ro.nicuch.tag.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ro.nicuch.tag.nbt.Tag;

public class TagLoadEvent<T extends Tag> extends Event {
    private final static HandlerList handlers = new HandlerList();
    private final T tag;

    public TagLoadEvent(T tag) {
        this.tag = tag;
    }

    public T getTag() {
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
