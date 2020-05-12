package ro.nicuch.tag.fallback;

import org.bukkit.ChatColor;
import ro.nicuch.tag.nbt.Tag;
import ro.nicuch.tag.nbt.TagIO;

import java.io.File;

public class CoruptedDataBackup {
    private final String coruptedDataId;
    private final File fileToWrite;
    private final Tag compoundTagToWrite;
    private final String world;

    protected CoruptedDataBackup(final String coruptedDataId, final File fileToWrite, final Tag compoundTagToWrite, String world) {
        this.coruptedDataId = coruptedDataId;
        this.fileToWrite = fileToWrite;
        this.compoundTagToWrite = compoundTagToWrite;
        this.world = world;
    }

    protected String tryToOverWrite() {
        try {
            TagIO.writeFile(this.compoundTagToWrite, fileToWrite);
            return "(Overwrite) File <" + (world == null ? "" : world + "/") + this.coruptedDataId + ".dat> overwrited!";
        } catch (Exception ioe) {
            ioe.printStackTrace();
            return ChatColor.RED + "(Overwrite) File <" + (world == null ? "" : world + "/") + this.coruptedDataId + ".dat> failed to overwrite!";
        }
    }

    protected String tryToWriteToBackup(final File directory) {
        try {
            TagIO.writeFile(this.compoundTagToWrite, new File(directory + File.separator + this.coruptedDataId + ".backup"));
            return "(Backup) File <" + (world == null ? "" : world + "/") + this.coruptedDataId + ".dat> backup'ed!";
        } catch (Exception ioe) {
            ioe.printStackTrace();
            return ChatColor.RED + "(Backup) File <" + world + "/" + this.coruptedDataId + ".backup> failed to backup!";
        }
    }
}
