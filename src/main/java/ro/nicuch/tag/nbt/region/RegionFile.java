package ro.nicuch.tag.nbt.region;

import org.jetbrains.annotations.Nullable;
import ro.nicuch.tag.nbt.ChunkCompoundTag;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile implements AutoCloseable {
    private static final int SECTOR_SIZE = 4096; // 4kb sectors
    private static final int CHUNKS_WIDTH = 32, CHUNKS_LENGTH = 32, CHUNKS_HEIGHT = 32; // a region file contains x32 x y32 x z32 chunks
    private static final int CHUNKS_TABLE_SIZE = CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH * 4; // chunks table contains each chunk big-endian int offset

    private File region_file;
    private FileChannel file_channel;
    private FileLock file_lock;
    //private final ReentrantLock lock = new ReentrantLock();

    private List<Boolean> sectors;
    private final IntBuffer offsets = IntBuffer.allocate(CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH);

    public RegionFile(final Path directory, final int region_x, final int region_y, final int region_z) {
        try {
            this.region_file = new File(directory + File.separator + "r." + region_x + "." + region_y + "." + region_z + ".tag");
            this.file_channel = FileChannel.open(this.region_file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
            if (this.file_channel.size() < CHUNKS_TABLE_SIZE) {
                this.file_channel.write(ByteBuffer.allocate(CHUNKS_TABLE_SIZE), 0);
            }
            long sectors_resize = this.file_channel.size() - CHUNKS_TABLE_SIZE;
            ByteBuffer growing_buffer = ByteBuffer.allocate(4096);
            if ((sectors_resize & 0xfff) != 0) {
                /* the file size is not a multiple of 4KB, grow it */
                for (int i = 0; i < ((sectors_resize + growing_buffer.position()) & 0xfff); i++)
                    growing_buffer.put((byte) 0);
            }
            this.file_channel.write(growing_buffer.flip(), this.file_channel.size());

            int total_sectors = (int) (((this.file_channel.size() - CHUNKS_TABLE_SIZE)) / SECTOR_SIZE);
            this.sectors = Collections.synchronizedList(new ArrayList<>(total_sectors));
            for (int i = 0; i < total_sectors; i++) {
                this.sectors.add(true);
            }

            ByteBuffer file_chunks_table = ByteBuffer.allocate(CHUNKS_TABLE_SIZE);
            this.file_channel.read(file_chunks_table, 0);
            file_chunks_table.flip();
            for (int i = 0; i < (32 * 32 * 32); i++) { // x, y, z
                int offset = file_chunks_table.getInt();
                this.offsets.put(i, offset);
                if ((offset >> 8) + (offset & 0xFF) <= this.sectors.size()) {             // I don't know how bitwise or shift works...
                    for (int sector_count = 0; sector_count < (offset & 0xFF); sector_count++) { // But i've done my math, and having the first 3 bytes for position
                        this.sectors.set((offset >> 8) + sector_count, false);                          // and last byte for sectors still works even tho there are ^32 more positions
                    }
                }
            }
            this.file_lock = this.file_channel.lock();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public final FileLock getLock() {
        return this.file_lock;
    }

    public final File getFile() {
        return this.region_file;
    }

    public final ChunkCompoundTag getChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z) {
        ChunkCompoundTag compoundTag = new ChunkCompoundTag();
        ByteBuffer buffer = this.readChunkData(chunk_x, chunk_y, chunk_z);
        if (buffer == null)
            return compoundTag;
        if (!buffer.hasArray())
            return compoundTag;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array())) {
            try (DataInputStream dis = new DataInputStream(bais)) {
                byte version = dis.readByte();
                if (version == CompressionType.GZIP.getVersion()) {
                    try (GZIPInputStream gzip = new GZIPInputStream(dis)) {
                        try (DataInputStream ret = new DataInputStream(gzip)) {
                            compoundTag.read(ret, 0);
                        }
                    }
                } else if (version == CompressionType.ZLIB.getVersion()) {
                    try (InflaterInputStream zlib = new InflaterInputStream(dis)) {
                        try (DataInputStream ret = new DataInputStream(zlib)) {
                            compoundTag.read(ret, 0);
                        }
                    }
                } else {
                    compoundTag.read(dis, 0);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return compoundTag;
    }

    public final void putChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z, final ChunkCompoundTag compoundTag) {
        this.putChunkCompoundTag(chunk_x, chunk_y, chunk_z, compoundTag, CompressionType.ZLIB); // default compression
    }

    public final void putChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z, final ChunkCompoundTag compoundTag, CompressionType compression) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (DataOutputStream dos = new DataOutputStream(baos)) {
                if (compression == CompressionType.GZIP) {
                    dos.writeByte(CompressionType.GZIP.getVersion());
                    try (GZIPOutputStream gzip = new GZIPOutputStream(dos)) {
                        try (DataOutputStream ret = new DataOutputStream(gzip)) {
                            compoundTag.write(ret);
                        }
                    }
                } else if (compression == CompressionType.ZLIB) {
                    dos.writeByte(CompressionType.ZLIB.getVersion());
                    try (DeflaterOutputStream zlib = new DeflaterOutputStream(dos)) {
                        try (DataOutputStream ret = new DataOutputStream(zlib)) {
                            compoundTag.write(ret);
                        }
                    }
                } else {
                    compoundTag.write(dos);
                }
            }
            byte[] array = baos.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(array);
            this.writeChunkData(chunk_x, chunk_y, chunk_z, buffer, array.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Nullable
    public final ByteBuffer readChunkData(final int chunk_x, final int chunk_y, final int chunk_z) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            throw new IllegalArgumentException("Chunk out of bounds");
        }
        try {
            int offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            if (offset == 0) {
                //System.out.println("Empty chunk!");
                return null;
            }

            int sector_number = offset >> 8;
            int sector_count = offset & 0xFF;

            if (sector_number + sector_count > sectors.size()) {
                throw new IllegalStateException("Invalid sector");
            }

            long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE);
            long size = position + ((long) sector_count * SECTOR_SIZE);

            //FileLock lock = this.file_channel.lock(position, size, true);

            ByteBuffer chunk_size_buffer = ByteBuffer.allocate(4);
            this.file_channel.read(chunk_size_buffer, position);
            int chunk_size = chunk_size_buffer.flip().getInt();

            if (chunk_size > SECTOR_SIZE * sector_count) {
                throw new IllegalStateException("Invalid chunk length");
            }

            ByteBuffer buffer = ByteBuffer.allocate(chunk_size);
            this.file_channel.read(buffer, position + 4);
            //lock.release();
            return buffer.flip(); // buffer ready for reading
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public final void writeChunkData(final int chunk_x, final int chunk_y, final int chunk_z, final ByteBuffer buffer, final int length) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            throw new IllegalArgumentException("Chunk out of bounds");
        }
        try {
            int offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            int sector_number = offset >> 8;
            int sectors_allocated = offset & 0xFF;
            int sectors_needed = ((length + 4) / SECTOR_SIZE) + 1;

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

                    //this.lock.lock();

                    for (int i = 0; i < sectors_needed; i++) {
                        this.file_channel.write(ByteBuffer.allocate(SECTOR_SIZE), this.file_channel.size());
                        sectors.add(false);
                    }

                    //this.lock.unlock();

                    this.writeData(sector_number, buffer, length, sectors_needed);
                    this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, (sector_number << 8) | sectors_needed);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeData(final int sector_number, final ByteBuffer buffer, final int length, final int sectors_needed) throws IOException {
        long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE);
        //FileLock lock = this.file_channel.lock(position, (long) sectors_needed * EMPTY_SECTOR_SIZE, true);

        this.file_channel.write(ByteBuffer.allocate(4).putInt(length).flip(), position);
        this.file_channel.write(buffer, position + 4);

        //lock.release();
    }

    public final boolean outOfBounds(final int x, final int y, final int z) {
        return x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32;
    }

    public final int getOffset(final int x, final int y, final int z) {
        return this.offsets.get((x + y * 32) + z * 1024);
    }

    public final void setOffset(final int x, final int y, final int z, final int offset) throws IOException {
        int location = (x + y * 32) + z * 1024;
        this.offsets.put(location, offset);

        long position = location * 4L;
        //FileLock lock = this.file_channel.lock(position, 4, true);
        this.file_channel.write(ByteBuffer.allocate(4).putInt(offset).flip(), position);
        //lock.release();
    }

    @Override
    public final void close() {
        try {
            this.file_channel.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
    }
}