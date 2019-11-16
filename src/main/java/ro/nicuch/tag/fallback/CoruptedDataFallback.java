package ro.nicuch.tag.fallback;

import ro.nicuch.lwjnbtl.CompoundTag;

import java.io.File;

public interface CoruptedDataFallback {
    String getCoruptedDataId();
    File getCoruptedDataFile();
    CompoundTag getCoruptedDataCompoundTag();
}
