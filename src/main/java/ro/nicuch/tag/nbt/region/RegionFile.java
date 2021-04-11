package ro.nicuch.tag.nbt.region;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanSortedMap;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.jetbrains.annotations.Nullable;
import ro.nicuch.test.nbt.CompoundTag;
import ro.nicuch.test.nbt.async.AsyncInt2BooleanSortedHashMap;
import ro.nicuch.test.nbt.async.AsyncInt2BooleanSortedMap;
import ro.nicuch.test.nbt.async.AsyncInt2ObjectHashMap;
import ro.nicuch.test.nbt.async.AsyncInt2ObjectMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile implements AutoCloseable {
    private static final int SECTOR_SIZE = 128; // sectors
    private static final int CHUNKS_WIDTH = 32, CHUNKS_LENGTH = 32, CHUNKS_HEIGHT = 32; // a region file contains x32 x y32 x z32 chunks\
    private static final int OFFSET_LENGTH = 6;
    private static final int CHUNKS_TABLE_SIZE = CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH * OFFSET_LENGTH;
    private static final int OFFSET_TABLE_LENGTH = (int) ((CHUNKS_HEIGHT * CHUNKS_LENGTH * CHUNKS_WIDTH) / Hash.DEFAULT_LOAD_FACTOR) + 1;

    private File region_file;
    private AsynchronousFileChannel file_channel;
    private FileLock file_lock;
    private AsyncInt2BooleanSortedMap sectors_free;
    private final AsyncInt2ObjectMap<RegionOffset> offsets = new AsyncInt2ObjectHashMap<>(OFFSET_TABLE_LENGTH, Hash.DEFAULT_LOAD_FACTOR);
    private final ReentrantLock[][][] regionLocks = new ReentrantLock[CHUNKS_WIDTH][CHUNKS_HEIGHT][CHUNKS_LENGTH];

    public RegionFile(final Path directory, final RegionID regionID) {
        this(directory, regionID.getX(), regionID.getY(), regionID.getZ());
    }

    public RegionFile(final Path directory, final int region_x, final int region_y, final int region_z) {
        try {
            this.region_file = new File(directory + File.separator + "r." + region_x + "." + region_y + "." + region_z + ".tag");
            this.file_channel = AsynchronousFileChannel.open(this.region_file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
            this.file_lock = this.file_channel.lock().get();
            ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
            Future<Void> futureFillRegionLocks = singleThreadExecutor.submit(() -> {
                for (int x = 0; x < CHUNKS_LENGTH; x++) {
                    for (int y = 0; y < CHUNKS_HEIGHT; y++) {
                        for (int z = 0; z < CHUNKS_WIDTH; z++) {
                            this.regionLocks[x][y][z] = new ReentrantLock();
                        }
                    }
                }
                return null;
            });
            if (this.file_channel.size() < CHUNKS_TABLE_SIZE)
                this.file_channel.write(ByteBuffer.allocate(CHUNKS_TABLE_SIZE), 0).get();
            long sectors_resize = this.file_channel.size() - CHUNKS_TABLE_SIZE;
            ByteBuffer growing_buffer = ByteBuffer.allocate(SECTOR_SIZE);
            while (((sectors_resize + growing_buffer.position()) % SECTOR_SIZE) != 0) {
                growing_buffer.put((byte) 0);
            }
            growing_buffer.flip();
            if (growing_buffer.hasRemaining())
                this.file_channel.write(growing_buffer, this.file_channel.size()).get();
            int total_sectors = (int) (((this.file_channel.size() - CHUNKS_TABLE_SIZE)) / SECTOR_SIZE); // can be 0 if no sectors exists
            if (total_sectors != 0)
                this.sectors_free = new AsyncInt2BooleanSortedHashMap((int) ((total_sectors / Hash.DEFAULT_LOAD_FACTOR) + 1), Hash.DEFAULT_LOAD_FACTOR);
            else
                this.sectors_free = new AsyncInt2BooleanSortedHashMap();
            Future<Void> futureFillFreeSectors = this.sectors_free.fill(0, total_sectors, true); // fill the sectors
            // don't wait for future, do other tasks
            ByteBuffer file_chunks_table = ByteBuffer.allocate(CHUNKS_TABLE_SIZE);
            this.file_channel.read(file_chunks_table, 0).get();
            file_chunks_table.flip();
            // now waiting for future sectors
            futureFillFreeSectors.get();
            for (int i = 0; i < (CHUNKS_WIDTH * CHUNKS_LENGTH * CHUNKS_HEIGHT); i++) { // x, y, z
                int sector = file_chunks_table.getInt(); // the starting sector of the data
                short sectors_size = file_chunks_table.getShort(); // the number of sectors the data covers
                RegionOffset offset = this.offsets.compute(i, (k, v) -> new RegionOffset(sector, sectors_size)).get();
                if ((!offset.isEmpty()) && sector + sectors_size <= this.sectors_free.size().get()) { // if offset is not empty and starting sector + sectors_size is less than or equals sectors_free.size,
                    for (int sector_count = 0; sector_count < sectors_size; sector_count++) {   // iterate over each sector from sectors_size and
                        this.sectors_free.replace(sector + sector_count, false).get();                    // set it not free
                    }
                }
            }
            // now waiting for future locks
            futureFillRegionLocks.get();
            singleThreadExecutor.shutdownNow();
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
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            throw new IllegalStateException("Chunk out of bounds!"); // might never happen
        }
        CompoundTag compoundTag = new CompoundTag();
        ReentrantLock lock = this.regionLocks[region_chunk_x][region_chunk_y][region_chunk_z];
        lock.lock();
        try {
            ByteBuffer buffer = this.readChunkData(region_chunk_x, region_chunk_y, region_chunk_z);
            if (buffer == null)
                return compoundTag;
            buffer.flip();
            if (!buffer.hasArray()) {
                return compoundTag;
            }
            if (!buffer.hasRemaining()) {
                return compoundTag;
            }
            try (FastByteArrayInputStream bais = new FastByteArrayInputStream(buffer.array())) {
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
        this.putChunkCompoundTag(chunk_x, chunk_y, chunk_z, compoundTag, CompressionType.ZLIB); // default compression
    }

    public final void putChunkCompoundTag(final int chunk_x, final int chunk_y, final int chunk_z, final CompoundTag compoundTag, CompressionType compression) {
        int region_chunk_x = chunk_x & 31;
        int region_chunk_y = chunk_y & 31;
        int region_chunk_z = chunk_z & 31;
        if (this.outOfBounds(region_chunk_x, region_chunk_y, region_chunk_z)) {
            throw new IllegalStateException("Chunk out of bounds!"); // might never happen
        }
        ReentrantLock lock = this.regionLocks[region_chunk_x][region_chunk_y][region_chunk_y];
        lock.lock();
        try {
            if (compoundTag.isEmpty()) {
                this.writeChunkData(region_chunk_x, region_chunk_y, region_chunk_z, null, 0, true); // this clears the free sectors of this chunk
                return;
            }
            try (FastByteArrayOutputStream baos = new FastByteArrayOutputStream()) {
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
                byte[] array = baos.array;
                this.writeChunkData(region_chunk_x, region_chunk_y, region_chunk_z, ByteBuffer.wrap(array), array.length, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public final ByteBuffer readChunkData(final int region_chunk_x, final int region_chunk_y, final int region_chunk_z) {
        try {
            RegionOffset offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            if (offset == null)
                throw new IllegalArgumentException("Offset can't be null!");
            if (offset.isEmpty()) { // offset is empty, that means the chunk should be too
                return null;
            }
            int sector_number = offset.getSector();
            int sectors_size = offset.getSectorsSize();
            if (sector_number + sectors_size > this.sectors_free.size().get()) // sectors shouldn't be greater than sectors_free.size
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
            ex.printStackTrace();
        }
        return null;
    }

    public final void writeChunkData(final int region_chunk_x, final int region_chunk_y, final int region_chunk_z, final ByteBuffer buffer, final int length, final boolean emptyChunk) {
        try {
            RegionOffset offset = this.getOffset(region_chunk_x, region_chunk_y, region_chunk_z);
            int sector_number = offset.getSector();
            short sectors_size = offset.getSectorsSize();
            if (emptyChunk) {
                /* mark the sectors previously used for this chunk as free */
                for (int i = 0; i < sectors_size; i++)
                    this.sectors_free.replace(sector_number + i, true).get();
                this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, new RegionOffset(sector_number, (short) 0)); // mark offset as empty
                return;
            }
            if (buffer == null)
                throw new IllegalArgumentException("Buffer can't be null");
            if (length <= 0)
                throw new IllegalArgumentException("Data length can't be less than 1");
            int sectors_needed = ((length + 4) / SECTOR_SIZE) + 1; // sectors needed means data length + 4 skipped bytes / SECTOR_SIZE (+ 1 because sectors needed can't be 0)
            if (sectors_needed >= Short.MAX_VALUE) // invalid length of sectors
                return;
            //noinspection StatementWithEmptyBody
            if (sectors_needed == sectors_size) { // sectors needed are equals to sectors_size, simply overwrite them
                //debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "rewrite");
            } else {
                // this si a hacky thing...that will slow us down =(
                // but can't do anything about it, as we need the sectors be synchronized
                ReentrantReadWriteLock.WriteLock writeLock = this.sectors_free.getLock().writeLock();
                ReentrantReadWriteLock.ReadLock readLock = this.sectors_free.getLock().readLock();
                writeLock.lock();
                readLock.lock();
                Int2BooleanSortedMap theActualMap = this.sectors_free.getMapDirectly();
                List<Future<Integer>> futuresEmtpySectors = new ArrayList<>();
                try {
                    /* we need to allocate new sectors or reallocate the existing ones */
                    /* mark the sectors previously used for this chunk as free */
                    for (int i = 0; i < sectors_size; i++)
                        theActualMap.replace(sector_number + i, true);
                    /* scan for a free space large enough to store this chunk */
                    ObjectSortedSet<Int2BooleanMap.Entry> entrySet = theActualMap.int2BooleanEntrySet();
                    int run_start = 0;
                    int run_length = 0;
                    int index = -1;
                    for (Int2BooleanMap.Entry entry : entrySet) {
                        index++;
                        boolean value = entry.getBooleanValue();
                        int key = entry.getIntKey();
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
                    if (run_length >= sectors_needed) {
                        /* we found a free space large enough */
                        //debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "reuse");
                        sector_number = run_start;
                        for (int i = 0; i < sectors_needed; i++)
                            theActualMap.replace(sector_number + i, false);
                    } else {
                        /* no free space large enough found -- we need to grow the file */
                        //debug("SAVE", region_chunk_x, region_chunk_y, region_chunk_z, length, "grow");
                        sector_number = theActualMap.size();
                        for (int i = 0; i < sectors_needed; i++) {
                            theActualMap.putIfAbsent(sector_number + i, false); // make it set
                            futuresEmtpySectors.add(this.file_channel.write(ByteBuffer.allocate(SECTOR_SIZE), ((long) (sector_number + i) * SECTOR_SIZE) + CHUNKS_TABLE_SIZE));
                        }
                    }
                } finally {
                    writeLock.unlock();
                    readLock.unlock();
                }
                // wait to finish
                for (Future<Integer> future : futuresEmtpySectors)
                    future.get();
                this.setOffset(region_chunk_x, region_chunk_y, region_chunk_z, new RegionOffset(sector_number, (short) sectors_needed));
                futuresEmtpySectors.clear(); // clear memory
            }
            this.writeData(sector_number, buffer, length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void writeData(final int sector_number, final ByteBuffer buffer, final int length) {
        try {
            long position = CHUNKS_TABLE_SIZE + ((long) sector_number * SECTOR_SIZE);
            Future<Integer> nonWaitingFuture = this.file_channel.write(ByteBuffer.allocate(4).putInt(length).flip(), position);
            this.file_channel.write(buffer, position + 4).get();
            nonWaitingFuture.get(); // doesn't wait until the main buffer is done, improvement: 0.0001%
            //this.file_channel.force(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public final boolean outOfBounds(final int x, final int y, final int z) {
        return x < 0 || x >= 32 || y < 0 || y >= 32 || z < 0 || z >= 32;
    }

    public final RegionOffset getOffset(final int x, final int y, final int z) {
        try {
            int location = x + (z * 32) + (y * 1024);
            return this.offsets.get(location).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new RegionOffset(0, (short) 0);
        }
    }

    public final void setOffset(final int x, final int y, final int z, final RegionOffset offset) {
        try {
            int location = x + (z * 32) + (y * 1024);
            Future<RegionOffset> nonWaitingFuture = this.offsets.replace(location, offset);
            long position = (long) location * OFFSET_LENGTH;
            this.file_channel.write(ByteBuffer.allocate(OFFSET_LENGTH).putInt(offset.getSector()).putShort(offset.getSectorsSize()).flip(), position).get();
            nonWaitingFuture.get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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