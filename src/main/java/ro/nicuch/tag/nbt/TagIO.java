package ro.nicuch.tag.nbt;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class TagIO {

    /**
     * Reads a compound tag from {@code file}.
     *
     * @param file the file
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readFile(final File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return readInputStream(stream);
        }
    }

    /**
     * Reads a compound tag from {@code path}.
     *
     * @param path the path
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readPath(final Path path) throws IOException {
        return readFile(path.toFile());
    }

    /**
     * Reads a compound tag from an input stream.
     *
     * @param input the input stream
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readInputStream(final InputStream input) throws IOException {
        try (final DataInputStream dis = new DataInputStream(input)) {
            return readDataInput(dis);
        }
    }

    /**
     * Reads a compound tag from {@code path} using GZIP decompression.
     *
     * @param path the path
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readCompressedPath(final Path path) throws IOException {
        return readCompressedFile(path.toFile());
    }

    /**
     * Reads a compound tag from {@code path} using GZIP decompression.
     *
     * @param file the file
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readCompressedFile(final File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return readCompressedInputStream(stream);
        }
    }

    /**
     * Reads a compound tag from an input stream using GZIP decompression.
     *
     * @param input the input stream
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readCompressedInputStream(final InputStream input) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(input)) {
            try (DataInputStream dis = new DataInputStream(gzipInputStream)) {
                return readDataInput(dis);
            }
        }
    }

    /**
     * Reads a compound tag from {@code input}.
     *
     * @param input the input
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static CompoundTag readDataInput(final DataInput input) throws IOException {
        TagType type = TagType.of(input.readByte());
        if (type != TagType.COMPOUND) {
            throw new IOException(String.format("Expected root tag to be a %s, was %s", TagType.COMPOUND, type));
        }
        input.skipBytes(input.readUnsignedShort()); // read empty name
        final Tag tag = type.create();
        tag.read(input, 0); // initial depth is zero
        return (CompoundTag) tag;
    }

    /**
     * Writes a compound tag to {@code file}.
     *
     * @param tag  the compound tag
     * @param file the file
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeFile(final CompoundTag tag, final File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            writeOutputStream(tag, out);
        }
    }

    /**
     * Writes a compound tag to {@code path}.
     *
     * @param tag  the compound tag
     * @param path the path
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writePath(final CompoundTag tag, final Path path) throws IOException {
        writeFile(tag, path.toFile());
    }

    /**
     * Writes a compound tag to an output stream.
     *
     * @param tag    the compound tag
     * @param output the output stream
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeOutputStream(final CompoundTag tag, final OutputStream output) throws IOException {
        try (final DataOutputStream dos = new DataOutputStream(output)) {
            writeDataOutput(tag, dos);
        }
    }

    /**
     * Writes a compound tag to {@code path} using GZIP compression.
     *
     * @param tag  the compound tag
     * @param path the path
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedPath(final CompoundTag tag, final Path path) throws IOException {
        writeCompressedFile(tag, path.toFile());
    }

    /**
     * Writes a compound tag to {@code path} using GZIP compression.
     *
     * @param tag  the compound tag
     * @param file the file
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedFile(final CompoundTag tag, final File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            writeCompressedOutputStream(tag, out);
        }
    }

    /**
     * Writes a compound tag to an output stream using GZIP compression.
     *
     * @param tag    the compound tag
     * @param output the output stream
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedOutputStream(final CompoundTag tag, final OutputStream output) throws IOException {
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(output)) {
            try (DataOutputStream dos = new DataOutputStream(gzipOutputStream)) {
                writeDataOutput(tag, dos);
            }
        }
    }

    /**
     * Writes a compound tag to {@code output}.
     *
     * @param tag    the compound tag
     * @param output the output
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeDataOutput(final CompoundTag tag, final DataOutput output) throws IOException {
        output.writeByte(tag.type().id());
        if (tag.type() != TagType.END) {
            output.writeUTF(""); // write empty name
            tag.write(output);
        }
    }
}
