package ro.nicuch.tag.nbt.region;

public class SectorFree {
    private final int sector_number, run_length;

    public SectorFree(final int sector_number, final int run_length) {
        if (sector_number < 0)
            throw new IllegalArgumentException("Sector number can't be less than 0");
        if (run_length < 0)
            throw new IllegalArgumentException("Run length can't be less than 0");
        this.sector_number = sector_number;
        this.run_length = run_length;
    }

    public final int getSectorNumber() {
        return this.sector_number;
    }

    public final int getRunLength() {
        return this.run_length;
    }
}
