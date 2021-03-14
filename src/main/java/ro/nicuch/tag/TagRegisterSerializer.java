package ro.nicuch.tag;

import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import ro.nicuch.tag.nbt.ChunkCompoundTag;
import ro.nicuch.tag.wrapper.ChunkUUID;

import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

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
        public void serialize(@NotNull DataOutput2 output2, @NotNull ChunkCompoundTag tag) throws IOException {
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                try (DeflaterOutputStream compressor = new DeflaterOutputStream(output)) {
                    try (DataOutputStream dos = new DataOutputStream(compressor)) {
                        tag.write(dos);
                    }
                }
                byte[] data = output.toByteArray();
                output2.write(data); // write data
            }
        }

        @Override
        public ChunkCompoundTag deserialize(@NotNull DataInput2 input2, int i) throws IOException {
            ChunkCompoundTag tag = new ChunkCompoundTag(); // empty tag
            // stupid way of converting
            byte[] data = new byte[i];
            input2.readFully(data);
            //
            try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
                try (InflaterInputStream decompressor = new InflaterInputStream(input)) {
                    try (DataInputStream dis = new DataInputStream(decompressor)) {
                        tag.read(dis, 0); // read data
                    }
                }
            }
            return tag;
        }
    };
}
