package ro.nicuch.tag.nbt.region;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class RegionFile {
    private static final ByteBuffer EMPTY_SECTOR = ByteBuffer.allocate(4096).flip(); // 4kb sectors
    private static final int CHUNKS_WIDTH = 32, CHUNKS_LENGTH = 32, CHUNKS_HEIGHT = 32; // a region file contains x32 x y32 x z32 chunks
    private static final int CHUNKS_TABLE_SIZE = CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH * 4; // chunks table contains each chunk big-endian int offset

    private File region_file;
    private FileInputStream fis;
    private FileChannel file_channel;
    private final ReentrantLock lock = new ReentrantLock();

    private List<Boolean> sectors;
    private final IntBuffer offsets = IntBuffer.allocate(CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH);

    public RegionFile(final Path directory, final int region_x, final int region_y, final int region_z) {
        try {
            this.region_file = new File(directory + File.separator + "r." + region_x + "." + region_y + "." + region_z + ".tag");
            this.fis = new FileInputStream(region_file);
            this.file_channel = this.fis.getChannel();

            if (this.file_channel.size() < CHUNKS_TABLE_SIZE) {
                this.file_channel.write(ByteBuffer.wrap(new byte[CHUNKS_TABLE_SIZE]));
            }
            long sectors_size = file_channel.size() - CHUNKS_TABLE_SIZE;
            ByteBuffer growing_buffer = ByteBuffer.allocate(4096);
            if ((sectors_size & 0xfff) != 0) {
                /* the file size is not a multiple of 4KB, grow it */
                growing_buffer.put((byte) 0);
            }
            this.file_channel.write(growing_buffer.flip(), 0);

            int total_sectors = (int) ((this.file_channel.size() - CHUNKS_TABLE_SIZE)) / EMPTY_SECTOR.capacity();
            this.sectors = Collections.synchronizedList(new ArrayList<>(total_sectors));

            ByteBuffer file_chunks_table = ByteBuffer.allocate(CHUNKS_TABLE_SIZE);
            this.file_channel.read(file_chunks_table, 0);
            file_chunks_table.flip();
            for (int i = 0; i < (32 * 32 * 32); i++) { // x, y, z
                int offset = file_chunks_table.getInt();
                offsets.put(i, offset);
                if (offset != 0 && (offset >> 8) + (offset & 0xFF) <= sectors.size()) {             // I don't know how bitwise or shift works...
                    for (int sector_number = 0; sector_number < (offset & 0xFF); sector_number++) { // But i've done my math, and having the first 3 bytes for position
                        sectors.set((offset >> 8) + sector_number, false);                          // and last byte for sectors still works even tho there are ^32 more positions
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    public ByteBuffer readChunkData(final int chunk_x, final int chunk_y, final int chunk_z) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            System.out.println("Chunk out of bounds!");
            return null;
        }
        try {
            int offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            if (offset == 0) {
                System.out.println("Empty chunk!");
                return null;
            }

            int sector_number = offset >> 8;
            int sector_count = offset & 0xFF;

            if (sector_number + sector_count > sectors.size()) {
                System.out.println("Invalid sector!");
                return null;
            }

            long position = CHUNKS_TABLE_SIZE + ((long) sector_number * EMPTY_SECTOR.capacity());
            long size = position + ((long) sector_count * EMPTY_SECTOR.capacity());

            FileLock lock = this.file_channel.lock(position, size, false);

            ByteBuffer chunk_size_buffer = ByteBuffer.allocate(4);
            this.file_channel.read(chunk_size_buffer, position);
            int chunk_size = chunk_size_buffer.flip().getInt();

            if (chunk_size > EMPTY_SECTOR.capacity() * sector_count) {
                System.out.println("Invalid lenght");
                lock.release();
                return null;
            }

            ByteBuffer buffer = ByteBuffer.allocate(chunk_size + 1);
            this.file_channel.read(buffer, position + 1);
            lock.release();
            return buffer.flip(); // buffer ready for reading
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void writeChunkData(final int chunk_x, final int chunk_y, final int chunk_z, final ByteBuffer buffer, final int length) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            System.out.println("Chunk out of bounds!");
            return;
        }
        try {
            int offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            int sector_number = offset >> 8;
            int sectors_allocated = offset & 0xFF;
            int sectors_needed = ((length + 4) / 4096) + 1;

            if (sectors_needed >= 256)
                return;

            if (sectors_needed == sectors_allocated) {
                /* we can simply overwrite the old sectors */
                this.writeData(sector_number, buffer, length, sectors_needed);
            } else {
                /* we need to allocate new sectors or reallocate the existing ones */

                /* mark the sectors previously used for this chunk as free */
                for (int i = 0; i < sectors_allocated; i++)
                    sectors.set(sector_number + i, true);

                /* scan for a free space large enough to store this chunk */
                int run_start = sectors.indexOf(true);
                int run_length = 0;
                if (run_start != -1) {
                    for (int i = run_start; i < sectors.size(); i++) {
                        if (run_length != 0) {
                            if (sectors.get(i))
                                run_length++;
                            else
                                run_length = 0;
                        } else if (sectors.get(i)) {
                            run_start = i;
                            run_length = 1;
                        }
                        if (run_length >= sectors_needed)
                            break;
                    }
                }

                if (run_length >= sectors_needed) {
                    /* we found a free space large enough */
                    sector_number = run_start;
                    this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, (sector_number << 8) | sectors_needed);
                    for (int i = 0; i < sectors_needed; i++)
                        sectors.set(sector_number + i, false);
                    this.writeData(sector_number, buffer, length, sectors_needed);

                } else {
                    /* no free space large enough found -- we need to grow the file */
                    sector_number = sectors.size();

                    this.lock.lock(); // locks the file growing for one thread at the time

                    for (int i = 0; i < sectors_needed; i++) {
                        this.file_channel.write(EMPTY_SECTOR, this.file_channel.size());
                        sectors.add(false);
                    }

                    this.lock.unlock(); // unlocks the growing, #writeData() has it's own lock

                    this.writeData(sector_number, buffer, length, sectors_needed);
                    this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, (sector_number << 8) | sectors_needed);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeData(final int sector_number, final ByteBuffer buffer, final int length, final int sectors_needed) throws IOException {
        long position = CHUNKS_TABLE_SIZE + ((long) sector_number * EMPTY_SECTOR.capacity());
        FileLock lock = this.file_channel.lock(position, (long) sectors_needed * EMPTY_SECTOR.capacity(), false);

        this.file_channel.write(ByteBuffer.allocate(1).putInt(length).flip(), position);
        this.file_channel.write(buffer, position + 4);

        lock.release();
    }

    public boolean outOfBounds(final int x, final int y, final int z) {
        return x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32;
    }

    public int getOffset(final int x, final int y, final int z) {
        return this.offsets.get((x + y * 32) + z * 1024);
    }

    public void setOffset(final int x, final int y, final int z, final int offset) {
        int location = (x + y * 32) + z * 1024;
        this.offsets.put(location, offset);
        // todo update chunk table inside file channel
    }

    public File getFile() {
        return this.region_file;
    }

    public void close() {
        try {
            this.fis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

