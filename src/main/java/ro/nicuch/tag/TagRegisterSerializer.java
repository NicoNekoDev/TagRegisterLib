package ro.nicuch.tag;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import ro.nicuch.tag.nbt.ChunkCompoundTag;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.IOException;

public interface TagRegisterSerializer {

    Serializer<ChunkUUID> CHUNK_SERIALIZER = new Serializer<ChunkUUID>() {
        @Override
        public void serialize(@NotNull DataOutput2 output, @NotNull ChunkUUID chunkUUID) throws IOException {
            output.writeInt(chunkUUID.getX());
            output.writeInt(chunkUUID.getZ());
        }

        @Override
        public ChunkUUID deserialize(@NotNull DataInput2 input, int i) throws IOException {
            return new ChunkUUID(input.readInt(), input.readInt());
        }
    };

    Serializer<ChunkCompoundTag> CHUNK_COMPOUND_TAG_SERIALIZER = new Serializer<ChunkCompoundTag>() {
        @Override
        public void serialize(@NotNull DataOutput2 output, @NotNull ChunkCompoundTag tag) throws IOException {
            tag.write(output);
        }

        @Override
        public ChunkCompoundTag deserialize(@NotNull DataInput2 input, int i) throws IOException {
            ChunkCompoundTag tag = new ChunkCompoundTag();
            tag.read(input, 0); //broken depth
            return tag;
        }
    };
}
