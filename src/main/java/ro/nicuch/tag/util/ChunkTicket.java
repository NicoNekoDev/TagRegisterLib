package ro.nicuch.tag.util;

import ro.nicuch.tag.wrapper.ChunkUUID;

import java.util.concurrent.TimeUnit;

public final class ChunkTicket {
    private final ChunkUUID chunkId;
    private final TicketType ticketType;
    private final int distance;
    private final long time;
    private final TimeUnit timeUnit;

    public ChunkTicket(ChunkUUID chunkId, TicketType ticketType, int distance, long time, TimeUnit timeUnit) {
        this.chunkId = chunkId;
        this.ticketType = ticketType;
        this.distance = distance;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public ChunkTicket(ChunkUUID chunkId, TicketType ticketType) {
        this(chunkId, ticketType, ticketType.getDefaultDistance(), ticketType.getDefaultTime(), TimeUnit.SECONDS);
    }

    public long getTime() {
        return this.time;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public int getDistance() {
        return this.distance;
    }

    public ChunkUUID getChunkId() {
        return this.chunkId;
    }

    public TicketType getTicketType() {
        return this.ticketType;
    }
}
