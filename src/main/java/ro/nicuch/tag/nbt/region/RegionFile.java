package ro.nicuch.test.nbt.region;

import org.jetbrains.annotations.Nullable;
import ro.nicuch.test.nbt.CompoundTag;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile implements AutoCloseable {
    private static final int SECTOR_SIZE = 128; // sectors
    private static final int CHUNKS_WIDTH = 32, CHUNKS_LENGTH = 32, CHUNKS_HEIGHT = 32; // a region file contains x32 x y32 x z32 chunks\
    private static final int OFFSET_LENGTH = 6;
    private static final int CHUNKS_TABLE_SIZE = CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH * OFFSET_LENGTH;

    private File region_file;
    private AsynchronousFileChannel file_channel;
    private FileLock file_lock;
    //private final ReentrantLock lock = new ReentrantLock();

    private ConcurrentMap<Integer, Boolean> sectors_free;
    private ConcurrentMap<Integer, RegionOffset> offsets;
    private final ReentrantLock[][][] regionLocks = new ReentrantLock[CHUNKS_WIDTH][CHUNKS_LENGTH][CHUNKS_HEIGHT];

    public RegionFile(final Path directory, final RegionID regionID) {
        this(directory, regionID.getX(), regionID.getY(), regionID.getZ());
    }

    public RegionFile(final Path directory, final int region_x, final int region_y, final int region_z) {
        try {
            this.region_file = new File(directory + File.separator + "r." + region_x + "." + region_y + "." + region_z + ".tag");
            debugln("REGION LOAD " + this.region_file);
            debugln("This region should only load once!");
            for (int x = 0; x < CHUNKS_WIDTH; x++) {
                for (int y = 0; y < CHUNKS_HEIGHT; y++) {
                    for (int z = 0; z < CHUNKS_LENGTH; z++) {
                        regionLocks[x][y][z] = new ReentrantLock();
                    }
                }
            }
            this.file_channel = AsynchronousFileChannel.open(this.region_file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
            this.file_lock = this.file_channel.lock().get();
            if (this.file_channel.size() < CHUNKS_TABLE_SIZE) {
                int write = this.file_channel.write(ByteBuffer.allocate(1), CHUNKS_TABLE_SIZE - 1).get();
                //this.file_channel.force(false); // no metadata
                debugln("Written bytes: " + write);
            }
            long sectors_resize = this.file_channel.size() - CHUNKS_TABLE_SIZE;
            ByteBuffer growing_buffer = ByteBuffer.allocate(SECTOR_SIZE);
            while (((sectors_resize + growing_buffer.position()) % SECTOR_SIZE) != 0) {
                debugln("Growing file size: " + growing_buffer.position());
                growing_buffer.put((byte) 0);
            }
            int write = this.file_channel.write(growing_buffer.flip(), this.file_channel.size()).get();
            //this.file_channel.force(false); // no metadata
            debugln("Written bytes to increase file to " + SECTOR_SIZE + ": " + write);
            int total_sectors = (int) (((this.file_channel.size() - CHUNKS_TABLE_SIZE)) / SECTOR_SIZE); // can be 0 if no sectors exists
            //this.sectors_free = Collections.synchronizedList(new ArrayList<>(total_sectors));
            this.sectors_free = new ConcurrentHashMap<>((int) ((total_sectors / 0.75f) + 1), 0.75f);
            for (int i = 0; i < total_sectors; i++) {
                this.sectors_free.put(i, true); // add all present sectors and make them all free
            }

            //this.offsets = Collections.synchronizedList(new ArrayList<>(CHUNKS_HEIGHT + CHUNKS_LENGTH + CHUNKS_WIDTH));
            this.offsets = new ConcurrentHashMap<>((int) (CHUNKS_HEIGHT + CHUNKS_LENGTH + CHUNKS_WIDTH / 0.90f) + 1, 0.90f);
            ByteBuffer file_chunks_table = ByteBuffer.allocate(CHUNKS_TABLE_SIZE);
            this.file_channel.read(file_chunks_table, 0).get();
            file_chunks_table.flip();
            for (int i = 0; i < (CHUNKS_WIDTH * CHUNKS_LENGTH * CHUNKS_HEIGHT); i++) { // x, y, z
                int sector = file_chunks_table.getInt(); // the starting sector of the data
                short sectors_size = file_chunks_table.getShort(); // the number of sectors the data covers
                //debugln("Offset position= <" + i + "> sector= <" + offset.getSector() + "> size= <" + offset.getSectorsSize() + ">");
                //this.offsets.put(i, offset);
                RegionOffset offset = this.offsets.compute(i, (k, v) -> new RegionOffset(sector, sectors_size));
                if ((!offset.isEmpty()) && sector + sectors_size <= this.sectors_free.size()) { // if offset is not empty and starting sector + sectors_size is less than or equals sectors_free.size,
                    for (int sector_count = 0; sector_count < sectors_size; sector_count++) {   // iterate over each sector from sectors_size and
                        this.sectors_free.put(sector + sector_count, false);                    // set it not free
                    }
                }
            }
            debugln("REGION LOAD", region_x, region_y, region_z, "FINISH");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final FileLock getLock() {
        return this.file_lock;
    }

    public final File getFile() {
        return this.region_file;
    }

    public final CompoundTag getChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;

        ReentrantLock lock = this.regionLocks[region_chunk_x][region_chunk_y][region_chunk_z];
        try {
            lock.lock();
            CompoundTag compoundTag = new CompoundTag();
            ByteBuffer buffer = this.readChunkData(chunk_x, chunk_y, chunk_z);
            if (buffer == null)
                return compoundTag;
            if (!buffer.hasArray()) {
                debugln("miss has array");
                return compoundTag;
            }
            if (!buffer.hasRemaining()) {
                debugln("miss remaining");
                return compoundTag;
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array())) {
                try (DataInputStream dis = new DataInputStream(bais)) {
                    CompressionType version = CompressionType.valueOf(dis.readByte());
                    switch (version) {
                        case ZLIB:
                            try (InflaterInputStream zlib = new InflaterInputStream(dis)) {
                                try (DataInputStream ret = new DataInputStream(zlib)) {
                                    compoundTag.read(ret, 0);
                                }
                            }
                            break;
                        case GZIP:
                            try (GZIPInputStream gzip = new GZIPInputStream(dis)) {
                                try (DataInputStream ret = new DataInputStream(gzip)) {
                                    compoundTag.read(ret, 0);
                                }
                            }
                            break;
                        default:
                            compoundTag.read(dis, 0);
                            break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return compoundTag;
        } finally {
            lock.unlock();
        }
    }

    public final void putChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z, final CompoundTag compoundTag) {
        this.putChunkCompoundTag(chunk_x, chunk_y, chunk_z, compoundTag, CompressionType.NONE); // default compression
    }

    public final void putChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z, final CompoundTag compoundTag, CompressionType compression) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;

        ReentrantLock lock = this.regionLocks[region_chunk_x][region_chunk_y][region_chunk_z];
        try {
            lock.lock();
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (DataOutputStream dos = new DataOutputStream(baos)) {
                    switch (compression) {
                        case ZLIB:
                            dos.writeByte(CompressionType.ZLIB.getVersion());
                            try (DeflaterOutputStream zlib = new DeflaterOutputStream(dos)) {
                                try (DataOutputStream ret = new DataOutputStream(zlib)) {
                                    compoundTag.write(ret);
                                    ret.flush();
                                }
                                zlib.flush();
                                zlib.finish();
                            }
                            break;
                        case GZIP:
                            dos.writeByte(CompressionType.GZIP.getVersion());
                            try (GZIPOutputStream gzip = new GZIPOutputStream(dos)) {
                                try (DataOutputStream ret = new DataOutputStream(gzip)) {
                                    compoundTag.write(ret);
                                    ret.flush();
                                }
                                gzip.flush();
                                gzip.finish();
                            }
                            break;
                        default:
                            dos.writeByte(CompressionType.NONE.getVersion());
                            compoundTag.write(dos);
                            break;
                    }
                    dos.flush();
                }
                baos.flush();
                byte[] array = baos.toByteArray();
                ByteBuffer buffer = ByteBuffer.wrap(array);
                this.writeChunkData(chunk_x, chunk_y, chunk_z, buffer, array.length);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            lock.unlock();
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
            RegionOffset offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);

            if (offset.isEmpty()) { // offset is empty, that means the chunk should be too
                debugln("READ", region_chunk_x, region_chunk_y, region_chunk_z, "miss");
                return null;
            }

            int sector_number = offset.getSector();
            int sectors_size = offset.getSectorsSize();

            if (sector_number + sectors_size > this.sectors_free.size()) { // sectors shouldn't be greater than sectors_free.size
                throw new IllegalStateException("Invalid sector");
            }

            long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE); // position where the sector should start

            ByteBuffer chunk_size_buffer = ByteBuffer.allocate(4);
            this.file_channel.read(chunk_size_buffer, position).get();
            int length = chunk_size_buffer.flip().getInt();

            if (length > SECTOR_SIZE * sectors_size) { // length can't be greater than sectors_size * sector_size
                throw new IllegalStateException("Invalid chunk length");
            }

            ByteBuffer buffer = ByteBuffer.allocate(length);
            this.file_channel.read(buffer, position + 4).get();// read data at position (skip 4 bytes for length value)
            //lock.release();
            return buffer.flip(); // buffer ready for reading
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private SectorFree findSectorFree(int sectors_needed) {
        Set<Map.Entry<Integer, Boolean>> entrySet = this.sectors_free.entrySet();
        int run_start = 0;
        int run_length = 0;

        int index = -1;
        for (Map.Entry<Integer, Boolean> entry : entrySet) {
            index++;
            boolean value = entry.getValue();
            if (run_length != 0) {
                if (value)
                    run_length++;
                else
                    run_length = 0;
            } else if (value) {
                run_start = index;
                run_length = 1;
            }
            if (run_length >= sectors_needed)
                break;
        }
        return new SectorFree(run_start, run_length);
    }

    public final void writeChunkData(final int chunk_x, final int chunk_y, final int chunk_z, final ByteBuffer buffer, final int length) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            throw new IllegalArgumentException("Chunk out of bounds");
        }
        try {

            // TODO skip empty chunks (clear them too)

            RegionOffset offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            int sector_number = offset.getSector();
            short sectors_size = offset.getSectorsSize();
            int sectors_needed = ((length + 4) / SECTOR_SIZE) + 1; // sectors needed means data lengh + 4 skipped bytes / SECTOR_SIZE (+ 1 because sectors needed can't be 0)
            debugln("sectors needed: " + sectors_needed);
            if (sectors_needed >= Short.MAX_VALUE) // invalid lengh of sectors
                return;

            if (sectors_needed == sectors_size) { // sectors needed are equals to sectors_size, simply overwrite them
                debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "rewrite");
                this.writeData(sector_number, buffer, length);
            } else {
                /* we need to allocate new sectors or reallocate the existing ones */

                /* mark the sectors previously used for this chunk as free */
                for (int i = 0; i < sectors_size; i++)
                    this.sectors_free.put(sector_number + i, true);

                /* scan for a free space large enough to store this chunk */

                SectorFree sectorFree = this.findSectorFree(sectors_needed);

                if (sectorFree.getRunLength() >= sectors_needed) {
                    /* we found a free space large enough */
                    debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "reuse");
                    sector_number = sectorFree.getSectorNumber();
                    this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, new RegionOffset(sector_number, (short) sectors_needed));
                    for (int i = 0; i < sectors_needed; i++)
                        this.sectors_free.put(sector_number + i, false);
                    this.writeData(sector_number, buffer, length);

                } else {
                    /* no free space large enough found -- we need to grow the file */
                    debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "grow");
                    sector_number = this.sectors_free.size();

                    //this.lock.lock();

                    for (int i = 0; i < sectors_needed; i++) {
                        this.sectors_free.put(sector_number + i, false); // make it set
                        int write = this.file_channel.write(ByteBuffer.allocate(1), ((long) (sector_number + i) * SECTOR_SIZE) + CHUNKS_TABLE_SIZE + (SECTOR_SIZE - 1)).get();
                        //this.file_channel.force(false);
                        debugln("Written (empty) bytes on file grow: " + write);
                    }

                    //this.lock.unlock();

                    this.writeData(sector_number, buffer, length);
                    this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, new RegionOffset(sector_number, (short) sectors_needed));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void writeData(final int sector_number, final ByteBuffer buffer, final int length) {
        try {
            debugln("sector number: " + sector_number);
            long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE);
            this.file_channel.write(ByteBuffer.allocate(4).putInt(length).flip(), position).get();
            this.file_channel.write(buffer, position + 4).get();
            //this.file_channel.force(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final boolean outOfBounds(final int x, final int y, final int z) {
        return x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32;
    }

    public final RegionOffset getOffset(final int x, final int y, final int z) {
        int location = (x + y * 32) + z * 1024;
        System.out.println("Location of offset is " + location);
        return this.offsets.get(location);
    }

    public final void setOffset(final int x, final int y, final int z, final RegionOffset offset) {
        try {
            int location = (x + y * 32) + z * 1024;
            this.offsets.put(location, offset);

            long position = (long) location * OFFSET_LENGTH;
            //FileLock lock = this.file_channel.lock(position, 4, true);
            this.file_channel.write(ByteBuffer.allocate(6).putInt(offset.getSector()).putShort(offset.getSectorsSize()).flip(), position).get();
            //this.file_channel.force(false);
            //lock.release();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void debugln(String in) {
        // System.out.println(in);
    }

    private void debug(String mode, int x, int y, int z, String in) {
        debugln("REGION " + mode + " " + region_file.getName() + "[" + x + "," + y + "," + z + "] = " + in);
    }

    private void debug(String mode, int x, int y, int z, int count, String in) {
        debugln("REGION " + mode + " " + region_file.getName() + "[" + x + "," + y + "," + z + "] " + count + "B = " + in);
    }

    private void debugln(String mode, int x, int y, int z, String in) {
        debug(mode, x, y, z, in);
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

        public static CompressionType valueOf(byte version) {
            switch (version) {
                case (byte) 1:
                    return CompressionType.GZIP;
                case (byte) 2:
                    return CompressionType.ZLIB;
                default:
                    return CompressionType.NONE;
            }
        }
    }
}