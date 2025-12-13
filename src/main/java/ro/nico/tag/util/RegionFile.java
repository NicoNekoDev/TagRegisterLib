package ro.nico.tag.util;

import org.jetbrains.annotations.Nullable;
import ro.nico.tag.CraftTagRegister;
import ro.nico.tag.nbt.ChunkCompoundTag;
import ro.nico.tag.wrapper.RegionPos;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class RegionFile implements AutoCloseable {
    private static final byte SECTOR_FREE = 0, SECTOR_NOT_FREE = 1;
    private static final int SECTOR_SIZE = 4096; // 4kb sectors
    private static final int CHUNKS_WIDTH = 32, CHUNKS_LENGTH = 32, CHUNKS_HEIGHT = 32; // a region file contains x32 x y32 x z32 chunks\
    private static final int OFFSET_LENGTH = 4;
    private static final int CHUNKS_TABLE_SIZE = CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH * OFFSET_LENGTH;
    private static final int OFFSET_TABLE_LENGTH = CHUNKS_HEIGHT * CHUNKS_LENGTH * CHUNKS_WIDTH;

    private File region_file;
    private AsynchronousFileChannel file_channel;
    private FileLock file_lock;
    private final ReentrantLock[][][] regionLocks = new ReentrantLock[CHUNKS_WIDTH][CHUNKS_HEIGHT][CHUNKS_LENGTH];
    private final AtomicInteger sectors_size = new AtomicInteger();
    private final AtomicIntegerArray offsets = new AtomicIntegerArray(OFFSET_TABLE_LENGTH);
    // 128 kb of space
    private final AtomicByteArray free_sectors = new AtomicByteArray(OFFSET_TABLE_LENGTH * 255); // initial with '0' , we don't need to fill sectors anymore! :D
    // almost 8 mb of space

    public RegionFile(final Path directory, final RegionPos regionID) {
        this(directory, regionID.getX(), regionID.getY(), regionID.getZ());
    }

    public RegionFile(final Path directory, final int region_x, final int region_y, final int region_z) {
        try {
            this.region_file = new File(directory + File.separator + "r." + region_x + "." + region_y + "." + region_z + ".tag");
            this.file_channel = AsynchronousFileChannel.open(this.region_file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
            this.file_lock = this.file_channel.lock().get();
            if (this.file_channel.size() < CHUNKS_TABLE_SIZE)
                this.file_channel.write(ByteBuffer.allocate(CHUNKS_TABLE_SIZE), 0).get();
            long sectors_resize = this.file_channel.size() - CHUNKS_TABLE_SIZE;
            ByteBuffer growing_buffer = ByteBuffer.allocate(SECTOR_SIZE);
            while (((sectors_resize + growing_buffer.position()) % SECTOR_SIZE) != 0)
                growing_buffer.put((byte) 0);
            growing_buffer.flip();
            if (growing_buffer.hasRemaining())
                this.file_channel.write(growing_buffer, this.file_channel.size()).get();
            int total_sectors = (int) (((this.file_channel.size() - CHUNKS_TABLE_SIZE)) / SECTOR_SIZE); // can be 0 if no sectors exists
            this.sectors_size.set(total_sectors);
            ByteBuffer file_chunks_table = ByteBuffer.allocate(CHUNKS_TABLE_SIZE);
            this.file_channel.read(file_chunks_table, 0).get();
            file_chunks_table.flip();
            for (int i = 0; i < (CHUNKS_WIDTH * CHUNKS_LENGTH * CHUNKS_HEIGHT); i++) { // x, y, z
                int offset = file_chunks_table.getInt();
                this.offsets.set(i, offset);
                int sector = offset >> 8; // the starting sector of the data
                int sectors_size = offset & 0xff; // the number of sectors the data covers

                if (offset != 0 && sector + sectors_size <= total_sectors) // if offset is not empty and starting sector + sectors_size is less than or equals sectors_free.size,
                    for (int sector_count = 0; sector_count < sectors_size; sector_count++)   // iterate over each sector from sectors_size and
                        this.free_sectors.set(sector + sector_count, SECTOR_NOT_FREE); // set it not free
            }
            for (int x = 0; x < CHUNKS_LENGTH; x++) {
                for (int y = 0; y < CHUNKS_HEIGHT; y++) {
                    for (int z = 0; z < CHUNKS_WIDTH; z++) {
                        this.regionLocks[x][y][z] = new ReentrantLock();
                    }
                }
            }
        } catch (Exception ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to initiate RegionFile", ex);
        }
    }

    public final FileLock getLock() {
        return this.file_lock;
    }

    public final File getFile() {
        return this.region_file;
    }

    public final ChunkCompoundTag getChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z))
            throw new IllegalStateException("Chunk out of bounds!"); // might never happen
        ChunkCompoundTag compoundTag = new ChunkCompoundTag(); // default chunk
        ReentrantLock lock = this.regionLocks[region_chunk_x][region_chunk_y][region_chunk_z];
        lock.lock();
        try {
            ByteBuffer buffer = this.readChunkData(region_chunk_x, region_chunk_y, region_chunk_z);
            if (buffer == null)
                return compoundTag;
            buffer.flip();
            if (!buffer.hasArray())
                return compoundTag;
            if (!buffer.hasRemaining())
                return compoundTag;
            return compoundTag.read(buffer, 512);
        } finally {
            lock.unlock();
        }
    }

    public final void putChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z, final ChunkCompoundTag compoundTag) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            throw new IllegalStateException("Chunk out of bounds!"); // might never happen
        }
        ReentrantLock lock = this.regionLocks[region_chunk_x][region_chunk_y][region_chunk_z];
        lock.lock();
        try {
            if (compoundTag.isEmpty(true)) {
                this.writeChunkData(region_chunk_x, region_chunk_y, region_chunk_z, null, true); // this clears the free sectors of this chunk
                return;
            }
            ByteBuffer buffer = ByteBuffer.allocate(compoundTag.bufferDataSize());
            compoundTag.write(buffer);
            this.writeChunkData(region_chunk_x, region_chunk_y, region_chunk_z, buffer.flip(), false);
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public final ByteBuffer readChunkData(final int region_chunk_x, final int region_chunk_y, final int region_chunk_z) {
        try {
            int offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            if (offset == 0) { // offset is empty, that means the chunk should be too
                return null;
            }
            int sector_number = offset >> 8;
            int sectors_size = offset & 0xff;
            if (sector_number + sectors_size > this.sectors_size.get()) // sectors shouldn't be greater than sectors_free.size
                throw new IllegalStateException("Invalid sector");
            long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE); // position where the sector should start
            ByteBuffer chunk_size_buffer = ByteBuffer.allocate(4);
            this.file_channel.read(chunk_size_buffer, position).get();
            int length = chunk_size_buffer.flip().getInt();
            if (length > SECTOR_SIZE * sectors_size) // length can't be greater than sectors_size * sector_size
                throw new IllegalStateException("Invalid chunk length");
            ByteBuffer buffer = ByteBuffer.allocate(length);
            this.file_channel.read(buffer, position + 4).get();// read data at position (skip 4 bytes for length value)
            return buffer;
        } catch (Exception ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to read chunk data", ex);
        }
        return null;
    }

    public final void writeChunkData(final int region_chunk_x, final int region_chunk_y, final int region_chunk_z, final ByteBuffer buffer, final boolean emptyChunk) {
        try {
            int offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            int sector_number = offset >> 8;
            int sectors_size = offset & 0xff;
            if (emptyChunk) {
                /* mark the sectors previously used for this chunk as free */
                for (int sector = 0; sector < sectors_size; sector++)
                    this.free_sectors.set(sector_number + sector, SECTOR_FREE);
                this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, 0); // mark offset as empty
                return;
            }
            if (buffer == null)
                throw new IllegalArgumentException("Buffer can't be null");
            if (!buffer.hasArray())
                throw new IllegalArgumentException("Buffer can't be empty!");
            int sectors_needed = ((buffer.array().length + 4) / SECTOR_SIZE) + 1; // sectors needed means data length + 4 skipped bytes / SECTOR_SIZE (+ 1 because sectors needed can't be 0)
            if (sectors_needed > 256) // invalid length of sectors
                return;
            //noinspection StatementWithEmptyBody
            if (sectors_needed == sectors_size) { // sectors needed are equals to sectors_size, simply overwrite them
                //debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "rewrite");
            } else {

                /* we need to allocate new sectors or reallocate the existing ones */
                /* mark the sectors previously used for this chunk as free */
                for (int sector = 0; sector < sectors_size; sector++)
                    this.free_sectors.set(sector_number + sector, SECTOR_FREE);
                /* scan for a free space large enough to store this chunk */
                int run_start = 0;
                int run_length = 0;
                for (int next = 0; next < this.free_sectors.length(); next++) {
                    if (this.free_sectors.get(next) == SECTOR_FREE) {
                        if (run_length != 0) {
                            run_length++;
                        } else {
                            run_start = next;
                            run_length = 1;
                        }
                    } else
                        run_length = 0;
                    if (run_length >= sectors_needed)
                        break;
                }
                if (run_length >= sectors_needed) {
                    /* we found a free space large enough */
                    //debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "reuse");
                    sector_number = run_start;
                } else {
                    /* no free space large enough found -- we need to grow the file */
                    //debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "grow");
                    sector_number = this.sectors_size.get();
                    // increase sectors size
                    this.sectors_size.addAndGet(sectors_needed);
                    // increase file size
                    for (int sector = 0; sector < sectors_needed; sector++)
                        this.file_channel.write(ByteBuffer.allocate(SECTOR_SIZE), ((long) (sector_number + sector) * SECTOR_SIZE) + CHUNKS_TABLE_SIZE);
                }
                /* mark allocated/new sectors as not free */
                for (int sector = 0; sector < sectors_needed; sector++)
                    this.free_sectors.set(sector_number + sector, SECTOR_NOT_FREE);
                this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, (sector_number << 8 | sectors_needed));
            }
            this.writeData(sector_number, buffer);
        } catch (Exception ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to write chunk data", ex);
        }
    }

    private void writeData(final int sector_number, final ByteBuffer buffer) throws ExecutionException, InterruptedException {
        long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE);
        Future<Integer> nonWaitingFuture = this.file_channel.write(ByteBuffer.allocate(4).putInt(buffer.array().length).flip(), position);
        this.file_channel.write(buffer, position + 4).get();
        nonWaitingFuture.get(); // doesn't wait until the main buffer is done, improvement: 0.0001%
        //this.file_channel.force(false);
    }

    public final boolean outOfBounds(final int x, final int y, final int z) {
        return x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32;
    }

    public final int getOffset(final int x, final int y, final int z) {
        int location = x + (z * 32) + (y * 1024);
        return this.offsets.get(location);
    }

    public final void setOffset(final int x, final int y, final int z, final int offset) throws ExecutionException, InterruptedException {
        int location = x + (z * 32) + (y * 1024);
        this.offsets.set(location, offset);
        long position = (long) location * OFFSET_LENGTH;
        this.file_channel.write(ByteBuffer.allocate(OFFSET_LENGTH).putInt(offset).flip(), position).get();
    }

    @Override
    public final void close() throws IOException {
        this.file_channel.close();
    }

    public enum CompressionType {
        NONE((byte) 0), ZLIB((byte) 2), GZIP((byte) 1);

        private final byte version;

        CompressionType(byte version) {
            this.version = version;
        }

        public byte getVersion() {
            return this.version;
        }

        public static CompressionType valueOf(byte version) {
            return switch (version) {
                case (byte) 1 -> CompressionType.GZIP;
                case (byte) 2 -> CompressionType.ZLIB;
                default -> CompressionType.NONE;
            };
        }
    }
}