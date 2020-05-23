package ro.nicuch.tag;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import ro.nicuch.tag.nbt.Tag;
import ro.nicuch.tag.nbt.TagType;
import ro.nicuch.tag.nbt.reg.ChunkCompoundTag;
import ro.nicuch.tag.wrapper.BlockUUID;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.IOException;

public interface TagRegisterSerializer {

    Serializer<ChunkUUID> CHUNK_SERIALIZER = new Serializer<>() {
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

    Serializer<BlockUUID> BLOCK_SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(@NotNull DataOutput2 output, @NotNull BlockUUID blockUUID) throws IOException {
            output.writeByte(blockUUID.getX());
            output.writeByte(blockUUID.getY());
            output.writeByte(blockUUID.getZ());
        }

        @Override
        public BlockUUID deserialize(@NotNull DataInput2 input, int i) throws IOException {
            return new BlockUUID(input.readByte(), input.readByte(), input.readByte());
        }
    };

    Serializer<Tag> TAG_SERIALIZER = new Serializer<>() {
        @Override
        public void serialize(@NotNull DataOutput2 output, @NotNull Tag tag) throws IOException {
            output.writeByte(tag.type().id());
            tag.write(output);
        }

        @Override
        public Tag deserialize(@NotNull DataInput2 input, int i) throws IOException {
            Tag tag = TagType.of(input.readByte()).create();
            tag.read(input, 0);
            return tag;
        }
    };

    Serializer<ChunkCompoundTag> CHUNK_COMPOUND_TAG_SERIALIZER = new Serializer<>() {
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
