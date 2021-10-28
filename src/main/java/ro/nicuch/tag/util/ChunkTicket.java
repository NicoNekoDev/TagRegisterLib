package ro.nicuch.tag.util;

import ro.nicuch.tag.wrapper.ChunkPos;

import java.util.concurrent.TimeUnit;

public final class ChunkTicket {
    private final ChunkPos chunkId;
    private final TicketType ticketType;
    private final int distance;
    private final long time;
    private final TimeUnit timeUnit;

    public ChunkTicket(ChunkPos chunkId, TicketType ticketType, int distance, long time, TimeUnit timeUnit) {
        this.chunkId = chunkId;
        this.ticketType = ticketType;
        this.distance = distance;
        this.time = time;
        this.timeUnit = timeUnit;
    }

    public ChunkTicket(ChunkPos chunkId, TicketType ticketType) {
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

    public ChunkPos getChunkId() {
        return this.chunkId;
    }

    public TicketType getTicketType() {
        return this.ticketType;
    }
}
