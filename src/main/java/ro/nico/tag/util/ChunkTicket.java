package ro.nico.tag.util;

import ro.nico.tag.wrapper.ChunkPos;

import java.util.concurrent.TimeUnit;

public record ChunkTicket(ChunkPos chunkId, TicketType ticketType, int distance, long time, TimeUnit timeUnit) {
    public ChunkTicket(ChunkPos chunkId, TicketType ticketType) {
        this(chunkId, ticketType, ticketType.getDefaultDistance(), ticketType.getDefaultTime(), TimeUnit.SECONDS);
    }
}
