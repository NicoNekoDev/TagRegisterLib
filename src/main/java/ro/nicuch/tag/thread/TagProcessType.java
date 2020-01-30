package ro.nicuch.tag.thread;

public enum TagProcessType {
    LOAD("load"), UNLOAD("unload");

    String name;

    TagProcessType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
