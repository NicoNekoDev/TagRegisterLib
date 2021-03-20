package ro.nicuch.tag.nbt.region;

import java.nio.ByteBuffer;

public class RegionOffset {
    private final int sector;
    private final short sectors_size;

    public RegionOffset(final int sector, final short sectors_size) {
        this.sector = sector;
        this.sectors_size = sectors_size;
    }

    public final int getSector() {
        return this.sector;
    }

    public final short getSectorsSize() {
        return this.sectors_size;
    }

    public final boolean isEmpty() {
        return this.sector == 0 && this.sectors_size == 0;
    }

    public final byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.putInt(this.sector);
        buffer.putShort(this.sectors_size);
        return buffer.array();
    }

    public static RegionOffset fromByteArray(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        int position = buffer.getInt();
        short sectors = buffer.getShort();
        return new RegionOffset(position, sectors);
    }
}
