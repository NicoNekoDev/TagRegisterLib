package ro.nicuch.tag.util;

import org.jetbrains.annotations.NotNull;
import ro.nicuch.tag.CraftTagRegister;

public class TicketType implements Comparable<TicketType> {
    public static TicketType PLAYER = new TicketType(0, 10, CraftTagRegister.MAXIMUM_DISTANCE);
    public static TicketType PORTAL = new TicketType(1, 5, 3);
    public static TicketType OTHER = new TicketType(2, 1, 1);

    private final int priority, defaultDistance;
    private final long defaultTime;

    private TicketType(int priority, long defaultTime, int defaultDistance) {
        this.priority = priority;
        this.defaultTime = defaultTime;
        this.defaultDistance = defaultDistance;
    }

    public final long getDefaultTime() {
        return this.defaultTime;
    }

    public final int getDefaultDistance() {
        return this.defaultDistance;
    }

    @Override
    public final int compareTo(@NotNull TicketType other) {
        return Integer.compare(other.priority, this.priority);
    }

}
