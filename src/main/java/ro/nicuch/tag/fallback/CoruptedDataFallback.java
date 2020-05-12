package ro.nicuch.tag.fallback;

import ro.nicuch.tag.nbt.Tag;

import java.io.File;

public interface CoruptedDataFallback {
    String getCoruptedDataId();
    File getCoruptedDataFile();
    Tag getCoruptedDataCompoundTag();
    String getWorldName();
}
