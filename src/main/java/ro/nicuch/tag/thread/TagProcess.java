package ro.nicuch.tag.thread;

import org.bukkit.Chunk;

public abstract class TagProcess {
    private final TagProcessType type;
    private final TagProcessUUID id;

    public TagProcess(final TagProcessType type, final Chunk chunk) {
        this.id = new TagProcessUUID(chunk, (this.type = type));
    }

    public abstract void run();

    public final TagProcessType getProcessType() {
        return this.type;
    }

    public final TagProcessUUID getProcessId() {
        return this.id;
    }
}
