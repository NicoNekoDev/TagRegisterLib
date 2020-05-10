package ro.nicuch.tag.fallback;

import ro.nicuch.tag.nbt.CompoundTag;

import java.io.File;

public interface CoruptedDataFallback {
    String getCoruptedDataId();
    File getCoruptedDataFile();
    CompoundTag getCoruptedDataCompoundTag();
    String getWorldName();
}
