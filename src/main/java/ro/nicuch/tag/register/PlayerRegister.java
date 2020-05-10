package ro.nicuch.tag.register;

import org.bukkit.Bukkit;
import ro.nicuch.tag.fallback.CoruptedDataFallback;
import ro.nicuch.tag.fallback.CoruptedDataManager;
import ro.nicuch.tag.nbt.CompoundTag;
import ro.nicuch.tag.nbt.TagIO;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PlayerRegister implements CoruptedDataFallback {
    //Let's hope this is the main world
    private final static String playersDir = Bukkit.getWorlds().get(0).getWorldFolder().getPath() + File.separator + "players_tags";

    static {
        File playersDirFile = new File(playersDir);
        if (!playersDirFile.exists())
            playersDirFile.mkdirs();
    }

    public static boolean isPlayerStored(UUID uuid) {
        return new File(playersDir + File.separator + uuid.toString() + ".dat").exists();
    }

    private final File playerFile;
    private final UUID uuid;
    private CompoundTag playerTag;

    public PlayerRegister(UUID uuid) {
        this.uuid = uuid;
        this.playerFile = new File(playersDir + File.separator + uuid.toString() + ".dat");
        this.load();
    }

    public CompoundTag getPlayerTag() {
        return this.playerTag;
    }

    public void setPlayerTag(CompoundTag tag) {
        this.playerTag = tag;
    }

    public void load() {
        if (!this.playerFile.exists()) {
            try {
                this.playerFile.createNewFile();
                this.playerFile.setReadable(true, false);
                this.playerFile.setWritable(true, false);
                this.playerTag = new CompoundTag();
                this.writePlayerFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.readPlayerFile();
    }

    public void readPlayerFile() {
        try {
            this.playerTag = TagIO.readFile(this.playerFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Reading) Player file <" + this.uuid.toString() + ".dat> is corupted!");
        }
    }

    public void writePlayerFile() {
        try {
            TagIO.writeFile(this.playerTag, this.playerFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("(Writing) Player file <" + this.uuid.toString() + ".dat> is corupted!");
            CoruptedDataManager.fallbackOperation(this);
        }
    }

    @Override
    public String getCoruptedDataId() {
        return this.uuid.toString() + "_player";
    }

    @Override
    public File getCoruptedDataFile() {
        return this.playerFile;
    }

    @Override
    public CompoundTag getCoruptedDataCompoundTag() {
        return this.playerTag;
    }

    @Override
    public String getWorldName() {
        return null; //Players don't need a world
    }

    public UUID getPlayerUUID() {
        return this.uuid;
    }
}
