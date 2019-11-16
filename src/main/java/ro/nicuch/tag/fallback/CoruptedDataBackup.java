package ro.nicuch.tag.fallback;

import org.bukkit.ChatColor;
import ro.nicuch.lwjnbtl.CompoundTag;
import ro.nicuch.lwjnbtl.TagIO;
import ro.nicuch.tag.TagRegister;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;

public class CoruptedDataBackup {
    private final String coruptedDataId;
    private final File fileToWrite;
    private final CompoundTag compoundTagToWrite;

    protected CoruptedDataBackup(final String coruptedDataId, final File fileToWrite, final CompoundTag compoundTagToWrite) {
        this.coruptedDataId = coruptedDataId;
        this.fileToWrite = fileToWrite;
        this.compoundTagToWrite = compoundTagToWrite;
    }

    protected String tryToOverWrite() {
        try {
            TagIO.writeOutputStream(this.compoundTagToWrite, new FileOutputStream(fileToWrite));
            return null;
        } catch (Exception ioe) {
            TagRegister.getLogger().log(Level.WARNING, "(OverWrite) File <" + this.coruptedDataId + ".dat> failed to overwrite!");
            return ChatColor.RED + "(OverWrite) File <" + this.coruptedDataId + ".dat> failed to overwrite!";
        }
    }

    protected String tryToWriteToBackup(final File directory) {
        try {
            TagIO.writeOutputStream(this.compoundTagToWrite, new FileOutputStream(new File(directory + File.separator + this.coruptedDataId + ".backup")));
            return null;
        } catch (Exception ioe) {
            TagRegister.getLogger().log(Level.WARNING, "(Backup) File <" + this.coruptedDataId + ".backup> failed to write!");
            return ChatColor.RED + "(Backup) File <" + this.coruptedDataId + ".backup> failed to write!";
        }
    }
}
