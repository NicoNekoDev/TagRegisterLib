package ro.nicuch.tag.thread;

public abstract class TagProcess {
    private final TagProcessType type;

    public TagProcess(final TagProcessType type) {
        this.type = type;
    }

    public abstract void run();

    public final TagProcessType getProcessType() {
        return this.type;
    }
}
