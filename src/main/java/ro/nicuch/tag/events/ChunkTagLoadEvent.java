package ro.nicuch.tag.events;

import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.register.ChunkRegister;

public class ChunkTagLoadEvent extends TagLoadEvent {
    private final ChunkRegister chunkRegister;

    public ChunkTagLoadEvent(ChunkRegister chunkRegister, CompoundTag tag) {
        super(tag);
        this.chunkRegister = chunkRegister;
    }

    public ChunkRegister getChunkRegister() {
        return this.chunkRegister;
    }
}
