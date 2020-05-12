package ro.nicuch.tag.events;

import ro.nicuch.tag.nbt.reg.ChunkCompoundTag;
import ro.nicuch.tag.register.ChunkRegister;

public class ChunkTagLoadEvent extends TagLoadEvent<ChunkCompoundTag> {
    private final ChunkRegister chunkRegister;

    public ChunkTagLoadEvent(ChunkRegister chunkRegister, ChunkCompoundTag tag) {
        super(tag);
        this.chunkRegister = chunkRegister;
    }

    public ChunkRegister getChunkRegister() {
        return this.chunkRegister;
    }
}
