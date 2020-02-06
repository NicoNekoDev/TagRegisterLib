package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.util.Objects;

public class TagProcessUUID {
    private final ChunkUUID chunkUUID;
    private final TagProcessType processType;

    public TagProcessUUID(Chunk chunk, TagProcessType processType) {
        this.chunkUUID = new ChunkUUID(chunk);
        this.processType = processType;
    }

    @Override
    public String toString() {
        return processType.toString() + "-" + chunkUUID.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TagProcessUUID))
            return false;
        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chunkUUID, this.processType);
    }
}
