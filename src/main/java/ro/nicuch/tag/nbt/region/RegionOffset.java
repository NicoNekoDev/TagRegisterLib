package ro.nicuch.tag.nbt.region;

public class RegionOffset {
    private final int sector;
    private final short sectors_size;

    public RegionOffset(final int sector, final short sectors_size) {
        if (sector < 0)
            throw new IllegalArgumentException("Sector can't be less than 0");
        if (sectors_size < 0)
            throw new IllegalArgumentException("Sectors size can't be less than 0");
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
        return this.sectors_size == 0;
    }
}
