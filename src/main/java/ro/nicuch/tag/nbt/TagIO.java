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
    public static Tag readFile(final File file, final TagType tagType) throws IOException {
        try (FileInputStream fileStream = new FileInputStream(file)) {
            try (BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {
                return readInputStream(bufferedStream, tagType);
            }
        }
    }

    /**
     * Reads a compound tag from {@code path}.
     *
     * @param path the path
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static Tag readPath(final Path path, final TagType tagType) throws IOException {
        return readFile(path.toFile(), tagType);
    }

    /**
     * Reads a compound tag from an input stream.
     *
     * @param input the input stream
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static Tag readInputStream(final InputStream input, final TagType tagType) throws IOException {
        try (final DataInputStream dis = new DataInputStream(input)) {
            return readDataInput(dis, tagType);
        }
    }

    /**
     * Reads a compound tag from {@code path} using GZIP decompression.
     *
     * @param path the path
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static Tag readCompressedPath(final Path path, final TagType tagType) throws IOException {
        return readCompressedFile(path.toFile(), tagType);
    }

    /**
     * Reads a compound tag from {@code path} using GZIP decompression.
     *
     * @param file the file
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static Tag readCompressedFile(final File file, final TagType tagType) throws IOException {
        try (FileInputStream fileStream = new FileInputStream(file)) {
            try (BufferedInputStream bufferedStream = new BufferedInputStream(fileStream)) {
                return readCompressedInputStream(bufferedStream, tagType);
            }
        }
    }

    /**
     * Reads a compound tag from an input stream using GZIP decompression.
     *
     * @param input the input stream
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static Tag readCompressedInputStream(final InputStream input, final TagType tagType) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(input)) {
            try (DataInputStream dis = new DataInputStream(gzipInputStream)) {
                return readDataInput(dis, tagType);
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
    public static Tag readDataInput(final DataInput input, TagType tagType) throws IOException {
        TagType type = TagType.of(input.readByte());
        if (type != tagType) {
            throw new IOException(String.format("Expected root tag to be a %s, was %s", tagType, type));
        }
        input.skipBytes(input.readUnsignedShort()); // read empty name
        final Tag tag = type.create();
        tag.read(input, 0); // initial depth is zero
        return tag;
    }

    /**
     * Writes a compound tag to {@code file}.
     *
     * @param tag  the compound tag
     * @param file the file
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeFile(final Tag tag, final File file) throws IOException {
        try (FileOutputStream fileStream = new FileOutputStream(file)) {
            try (BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream)) {
                writeOutputStream(tag, bufferedStream);
            }
        }
    }

    /**
     * Writes a compound tag to {@code path}.
     *
     * @param tag  the compound tag
     * @param path the path
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writePath(final Tag tag, final Path path) throws IOException {
        writeFile(tag, path.toFile());
    }

    /**
     * Writes a compound tag to an output stream.
     *
     * @param tag    the compound tag
     * @param output the output stream
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeOutputStream(final Tag tag, final OutputStream output) throws IOException {
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
    public static void writeCompressedPath(final Tag tag, final Path path) throws IOException {
        writeCompressedFile(tag, path.toFile());
    }

    /**
     * Writes a compound tag to {@code path} using GZIP compression.
     *
     * @param tag  the compound tag
     * @param file the file
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedFile(final Tag tag, final File file) throws IOException {
        try (FileOutputStream fileStream = new FileOutputStream(file)) {
            try (BufferedOutputStream bufferedStream = new BufferedOutputStream(fileStream)) {
                writeCompressedOutputStream(tag, bufferedStream);
            }
        }
    }

    /**
     * Writes a compound tag to an output stream using GZIP compression.
     *
     * @param tag    the compound tag
     * @param output the output stream
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedOutputStream(final Tag tag, final OutputStream output) throws IOException {
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
    public static void writeDataOutput(final Tag tag, final DataOutput output) throws IOException {
        output.writeByte(tag.type().id());
        if (tag.type() != TagType.END) {
            output.writeUTF(""); // write empty name
            tag.write(output);
        }
    }
}
