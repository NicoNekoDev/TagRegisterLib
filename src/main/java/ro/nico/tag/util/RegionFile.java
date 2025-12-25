package ro.nico.tag.util;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import lombok.Cleanup;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import org.jetbrains.annotations.Nullable;
import ro.nico.tag.CraftTagRegister;
import ro.nico.tag.nbt.tags.collection.CompoundTag;
import ro.nico.tag.wrapper.RegionPos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile implements AutoCloseable {
    private static final byte SECTOR_FREE = 0, SECTOR_NOT_FREE = 1;
    private static final int SECTOR_SIZE = 4096; // 4kb sectors
    private static final int CHUNKS_WIDTH = 32, CHUNKS_LENGTH = 32, CHUNKS_HEIGHT = 32; // a region file contains x32 x y32 x z32 chunks\
    private static final int OFFSET_LENGTH = 4;
    private static final int CHUNKS_TABLE_SIZE = CHUNKS_HEIGHT * CHUNKS_WIDTH * CHUNKS_LENGTH * OFFSET_LENGTH;
    private static final int OFFSET_TABLE_LENGTH = CHUNKS_HEIGHT * CHUNKS_LENGTH * CHUNKS_WIDTH;

    private File regionFile;
    private AsynchronousFileChannel fileChannel;
    private FileLock fileLock;
    private final ReentrantLock[][][] regionLocks = new ReentrantLock[CHUNKS_WIDTH][CHUNKS_HEIGHT][CHUNKS_LENGTH];
    private final AtomicInteger sectorsSize = new AtomicInteger();
    private final AtomicIntegerArray offsets = new AtomicIntegerArray(OFFSET_TABLE_LENGTH);
    // 128 kb of space
    private final AtomicByteArray freeSectors = new AtomicByteArray(OFFSET_TABLE_LENGTH * 255); // initial with '0' , we don't need to fill sectors anymore! :D
    // almost 8 mb of space

    public RegionFile(final Path directory, final RegionPos regionID) {
        this(directory, regionID.getX(), regionID.getY(), regionID.getZ());
    }

    public RegionFile(final Path directory, final int regionX, final int regionY, final int regionZ) {
        try {
            this.regionFile = new File(directory + File.separator + "r." + regionX + "." + regionY + "." + regionZ + ".tag");
            this.fileChannel = AsynchronousFileChannel.open(this.regionFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
            this.fileLock = this.fileChannel.lock().get(30, TimeUnit.SECONDS);

            if (this.fileChannel.size() < CHUNKS_TABLE_SIZE)
                this.fileChannel.write(ByteBuffer.allocate(CHUNKS_TABLE_SIZE), 0).get();

            long sectorsResize = this.fileChannel.size() - CHUNKS_TABLE_SIZE;
            ByteBuffer growingBuffer = ByteBuffer.allocate(SECTOR_SIZE);
            while (((sectorsResize + growingBuffer.position()) % SECTOR_SIZE) != 0)
                growingBuffer.put((byte) 0);
            growingBuffer.flip();
            if (growingBuffer.hasRemaining())
                this.fileChannel.write(growingBuffer, this.fileChannel.size()).get();

            int totalSectors = (int) (((this.fileChannel.size() - CHUNKS_TABLE_SIZE)) / SECTOR_SIZE); // can be 0 if no sectors exists
            this.sectorsSize.set(totalSectors);

            ByteBuffer fileChunksTable = ByteBuffer.allocate(CHUNKS_TABLE_SIZE);
            this.fileChannel.read(fileChunksTable, 0).get();
            fileChunksTable.flip();

            for (int i = 0; i < (CHUNKS_WIDTH * CHUNKS_LENGTH * CHUNKS_HEIGHT); i++) { // x, y, z
                int offset = fileChunksTable.getInt();
                this.offsets.set(i, offset);
                int sector = offset >> 8; // the starting sector of the data
                int sectorsSize = offset & 0xff; // the number of sectors the data covers

                if (offset != 0 && sector + sectorsSize <= totalSectors) // if offset is not empty and starting sector + sectorsSize is less than or equals sectors_free.size,
                    for (int sector_count = 0; sector_count < sectorsSize; sector_count++)   // iterate over each sector from sectorsSize and
                        this.freeSectors.set(sector + sector_count, SECTOR_NOT_FREE); // set it not free
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
        return this.fileLock;
    }

    public final File getFile() {
        return this.regionFile;
    }

    @SuppressWarnings("deprecation")
    public final CompoundTag getChunkCompoundTag(final int chunkX, final int chunkY, final int chunkZ) {
        int regionChunkX = chunkX & 31;
        int regionChunkY = chunkY & 31;
        int regionChunkZ = chunkZ & 31;
        if (this.outOfBounds(regionChunkX, regionChunkY, regionChunkZ))
            throw new IllegalStateException("Chunk out of bounds!"); // might never happen
        CompoundTag compoundTag = new CompoundTag();
        ReentrantLock lock = this.regionLocks[regionChunkX][regionChunkY][regionChunkZ];
        lock.lock();
        try {
            ByteBuffer buffer = this.readChunkData(regionChunkX, regionChunkY, regionChunkZ);
            if (buffer == null)
                return compoundTag;
            buffer.flip();
            if (!buffer.hasArray())
                return compoundTag;
            if (!buffer.hasRemaining())
                return compoundTag;

            @Cleanup FastByteArrayInputStream bais = new FastByteArrayInputStream(buffer.array());
            CompressionType compression = CompressionType.valueOf(bais.readByte());

            @Cleanup FastBufferedInputStream fbis = switch (compression) {
                case ZSTD -> new FastBufferedInputStream(new ZstdInputStream(bais));
                case GZIP -> new FastBufferedInputStream(new GZIPInputStream(bais));
                case ZLIB -> new FastBufferedInputStream(new InflaterInputStream(bais));
                case LZ4 -> new FastBufferedInputStream(new LZ4BlockInputStream(bais));
                case NONE -> new FastBufferedInputStream(bais);
            };

            @Cleanup DataInputStream dis = new DataInputStream(fbis);

            return compoundTag.read(dis, 512);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
        return compoundTag;
    }

    public final void putChunkCompoundTag(final int chunkX, final int chunkY, final int chunkZ, CompoundTag compoundTag) {
        this.putChunkCompoundTag(chunkX, chunkY, chunkZ, compoundTag, CompressionType.ZSTD);
    }

    public final void putChunkCompoundTag(final int chunkX, final int chunkY, final int chunkZ, CompoundTag compoundTag, CompressionType compression) {
        int regionChunkX = chunkX & 31;
        int regionChunkY = chunkY & 31;
        int regionChunkZ = chunkZ & 31;
        if (this.outOfBounds(regionChunkX, regionChunkY, regionChunkZ)) {
            throw new IllegalStateException("Chunk out of bounds!"); // might never happen
        }
        ReentrantLock lock = this.regionLocks[regionChunkX][regionChunkY][regionChunkZ];
        lock.lock();
        try {
            if (compoundTag.isEmpty()) {
                this.writeChunkData(regionChunkX, regionChunkY, regionChunkZ, null, true); // this clears the free sectors of this chunk
                return;
            }

            @Cleanup FastByteArrayOutputStream baos = new FastByteArrayOutputStream();
            baos.writeByte(compression.getVersion());

            @Cleanup FastBufferedOutputStream fbos = switch (compression) {
                case ZSTD -> new FastBufferedOutputStream(new ZstdOutputStream(baos, 3));
                case GZIP -> new FastBufferedOutputStream(new GZIPOutputStream(baos));
                case ZLIB -> new FastBufferedOutputStream(new DeflaterOutputStream(baos));
                case LZ4 -> new FastBufferedOutputStream(new LZ4BlockOutputStream(baos));
                case NONE -> new FastBufferedOutputStream(baos);
            };

            @Cleanup DataOutputStream dos = new DataOutputStream(fbos);

            compoundTag.write(dos, 512);

            ByteBuffer buffer = ByteBuffer.wrap(baos.array);
            this.writeChunkData(regionChunkX, regionChunkY, regionChunkZ, buffer.flip(), false);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public final ByteBuffer readChunkData(final int regionChunkX, final int regionChunkY, final int regionChunkZ) {
        try {
            int offset = this.getOffset(regionChunkX, regionChunkY, regionChunkZ);
            if (offset == 0) { // offset is empty, that means the chunk should be too
                return null;
            }
            int sectorNumber = offset >> 8;
            int sectorsSize = offset & 0xff;
            if (sectorNumber + sectorsSize > this.sectorsSize.get()) // sectors shouldn't be greater than sectors_free.size
                throw new IllegalStateException("Invalid sector");
            long position = CHUNKS_TABLE_SIZE + ((long) sectorNumber * SECTOR_SIZE); // position where the sector should start
            ByteBuffer chunkSizeBuffer = ByteBuffer.allocate(4);
            this.fileChannel.read(chunkSizeBuffer, position).get();
            int length = chunkSizeBuffer.flip().getInt();
            if (length > SECTOR_SIZE * sectorsSize) // length can't be greater than sectorsSize * sector_size
                throw new IllegalStateException("Invalid chunk length");
            ByteBuffer buffer = ByteBuffer.allocate(length);
            this.fileChannel.read(buffer, position + 4).get();// read data at position (skip 4 bytes for length value)
            return buffer;
        } catch (Exception ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to read chunk data", ex);
        }
        return null;
    }

    public final void writeChunkData(final int regionChunkX, final int regionChunkY, final int regionChunkZ, final ByteBuffer buffer, final boolean emptyChunk) {
        try {
            int offset = this.getOffset(regionChunkX, regionChunkY, regionChunkZ);
            int sectorNumber = offset >> 8;
            int sectorsSize = offset & 0xff;
            if (emptyChunk) {
                /* mark the sectors previously used for this chunk as free */
                for (int sector = 0; sector < sectorsSize; sector++)
                    this.freeSectors.set(sectorNumber + sector, SECTOR_FREE);
                this.setOffset(regionChunkX, regionChunkY, regionChunkZ, 0); // mark offset as empty
                return;
            }
            if (buffer == null)
                throw new IllegalArgumentException("Buffer can't be null");
            if (!buffer.hasArray())
                throw new IllegalArgumentException("Buffer can't be empty!");
            int sectorsNeeded = ((buffer.array().length + 4) / SECTOR_SIZE) + 1; // sectors needed means data length + 4 skipped bytes / SECTOR_SIZE (+ 1 because sectors needed can't be 0)
            if (sectorsNeeded > 256) // invalid length of sectors
                return;
            //noinspection StatementWithEmptyBody
            if (sectorsNeeded == sectorsSize) { // sectors needed are equals to sectorsSize, simply overwrite them
                //debug("SAVE", regionChunkX, regionChunkY, regionChunkZ, length, "rewrite");
            } else {

                /* we need to allocate new sectors or reallocate the existing ones */
                /* mark the sectors previously used for this chunk as free */
                for (int sector = 0; sector < sectorsSize; sector++)
                    this.freeSectors.set(sectorNumber + sector, SECTOR_FREE);
                /* scan for a free space large enough to store this chunk */
                int runStart = 0;
                int runLength = 0;
                for (int next = 0; next < this.freeSectors.length(); next++) {
                    if (this.freeSectors.get(next) == SECTOR_FREE) {
                        if (runLength != 0) {
                            runLength++;
                        } else {
                            runStart = next;
                            runLength = 1;
                        }
                    } else
                        runLength = 0;
                    if (runLength >= sectorsNeeded)
                        break;
                }
                if (runLength >= sectorsNeeded) {
                    /* we found a free space large enough */
                    //debug("SAVE", regionChunkX, regionChunkY, regionChunkZ, length, "reuse");
                    sectorNumber = runStart;
                } else {
                    /* no free space large enough found -- we need to grow the file */
                    //debug("SAVE", regionChunkX, regionChunkY, regionChunkZ, length, "grow");
                    sectorNumber = this.sectorsSize.get();
                    // increase sectors size
                    this.sectorsSize.addAndGet(sectorsNeeded);
                    // increase file size
                    for (int sector = 0; sector < sectorsNeeded; sector++)
                        this.fileChannel.write(ByteBuffer.allocate(SECTOR_SIZE), ((long) (sectorNumber + sector) * SECTOR_SIZE) + CHUNKS_TABLE_SIZE);
                }
                /* mark allocated/new sectors as not free */
                for (int sector = 0; sector < sectorsNeeded; sector++)
                    this.freeSectors.set(sectorNumber + sector, SECTOR_NOT_FREE);
                this.setOffset(regionChunkX, regionChunkY, regionChunkZ, (sectorNumber << 8 | sectorsNeeded));
            }
            this.writeData(sectorNumber, buffer);
        } catch (Exception ex) {
            CraftTagRegister.getLogger().log(Level.SEVERE, "Failed to write chunk data", ex);
        }
    }

    private void writeData(final int sectorNumber, final ByteBuffer buffer) throws ExecutionException, InterruptedException {
        long position = CHUNKS_TABLE_SIZE + ((long) sectorNumber * SECTOR_SIZE);
        Future<Integer> nonWaitingFuture = this.fileChannel.write(ByteBuffer.allocate(4).putInt(buffer.array().length).flip(), position);
        this.fileChannel.write(buffer, position + 4).get();
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
        if (this.outOfBounds(x, y, z))
            throw new RuntimeException("Out of bounds!");
        int location = x + (z * 32) + (y * 1024);
        this.offsets.set(location, offset);
        long position = (long) location * OFFSET_LENGTH;
        this.fileChannel.write(ByteBuffer.allocate(OFFSET_LENGTH).putInt(offset).flip(), position).get();
    }

    @Override
    public final void close() throws IOException {
        this.fileChannel.close();
        if (this.fileLock != null && this.fileLock.isValid())
            this.fileLock.release();
    }

    public enum CompressionType {
        NONE(0), ZLIB(2), GZIP(1), ZSTD(3), LZ4(4);

        private final int version;

        CompressionType(int version) {
            this.version = version;
        }

        public int getVersion() {
            return this.version;
        }

        public static CompressionType valueOf(int version) {
            return switch (version) {
                case 1 -> CompressionType.GZIP;
                case 2 -> CompressionType.ZLIB;
                case 3 -> CompressionType.ZSTD;
                case 4 -> CompressionType.LZ4;
                default -> CompressionType.NONE;
            };
        }
    }
}