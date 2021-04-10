package io.github.merykitty.slpprocessor.common;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.merykitty.slpprocessor.image.PaletteContainer;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import io.github.merykitty.slpprocessor.slpv3.SLPFileVer3;
import io.github.merykitty.slpprocessor.slpv4.SLPFileVer4;
import io.github.merykitty.slpprocessor.misc.*;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SLPFiles {
    private static final long VERSION_OFFSET = 0;
    private static final long VERSION_SIZE = 4;
    private static final long COMPRESSED_EXPECTED_SIZE_OFFSET = 4;
    private static final long COMPRESSED_COMPRESSED_FILE_OFFSET = 8;

    private SLPFiles() throws InstantiationException {
        throw new InstantiationException();
    }

    public static SLPFile decode(Path path, PaletteContainer palettes) throws IOException {
        var file = MemorySegment.mapFile(path, 0, Files.size(path), FileChannel.MapMode.READ_ONLY);
        return processSegment(file, true, palettes);
    }

    public static void encode(Path path, PaletteContainer palettes, SLPFile data, boolean doCompress) throws IOException {
        Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        Files.createFile(path);
        try (var uncompressed = data.toNativeData(palettes)) {
            if (doCompress) {
                try (var compressed = LZ4Compressor.compress(uncompressed);
                        var target = MemorySegment.mapFile(path, 0, compressed.byteSize() + COMPRESSED_COMPRESSED_FILE_OFFSET, FileChannel.MapMode.READ_WRITE);) {
                    MemoryAccess.setByteAtOffset(target, 0, (byte)'4');
                    MemoryAccess.setByteAtOffset(target, 1, (byte)'.');
                    MemoryAccess.setByteAtOffset(target, 2, (byte)'2');
                    MemoryAccess.setByteAtOffset(target, 3, (byte)'P');
                    MemoryAccess.setIntAtOffset(target, COMPRESSED_EXPECTED_SIZE_OFFSET, ByteOrder.LITTLE_ENDIAN, (int)uncompressed.byteSize());
                    target.asSlice(COMPRESSED_COMPRESSED_FILE_OFFSET).copyFrom(compressed);
                }
            } else {
                try (var target = MemorySegment.mapFile(path, 0, uncompressed.byteSize(), FileChannel.MapMode.READ_WRITE)) {
                    target.copyFrom(uncompressed);
                }
            }
        }
    }

    public static SLPFile importGraphics(Path importFolder, PaletteContainer palettes) throws IOException {
        var meta = new JSONObject(new JSONTokener(Files.readString(importFolder.resolve("meta.json"))));
        var version = Version.of(meta.getString("version"));
        if (version.major() == 4) {
            return SLPFileVer4.importGraphics(meta, importFolder, palettes);
        } else {
            return SLPFileVer3.importGraphics(meta, importFolder, palettes);
        }
    }

    public static long roundUpMod16(long in) {
        return ((in - 1) & 0xfffffffffffffff0L) + 0x10;
    }

    public static long roundUpMod32(long in) {
        return ((in - 1) & 0xffffffffffffffe0L) + 0x20;
    }

    private static SLPFile processSegment(MemorySegment file, boolean maybeCompressed, PaletteContainer palettes) {
        try (var data = file) {
            var version = Version.ofNativeData(data.asSlice(VERSION_OFFSET, VERSION_SIZE));
            int versionMajor = version.major();
            int versionMinor = version.minor();
            if (versionMajor < 4) {
                return SLPFileVer3.ofNativeData(data, palettes);
            } else if (versionMajor == 4) {
                if (!maybeCompressed) {
                    return SLPFileVer4.ofNativeData(data, palettes);
                } else {
                    if (versionMinor < 2) {
                        return SLPFileVer4.ofNativeData(data, palettes);
                    } else if (versionMinor == 2) {
                        long expectedSize = new uint(MemoryAccess.getIntAtOffset(data, COMPRESSED_EXPECTED_SIZE_OFFSET, ByteOrder.LITTLE_ENDIAN)).value();
                        var decompressedFile = LZ4Compressor.decompress(data.asSlice(COMPRESSED_COMPRESSED_FILE_OFFSET), expectedSize);
                        return processSegment(decompressedFile, false, palettes);
                    } else {
                        throw new RuntimeException("Unsupported version: " + version);
                    }
                }
            } else {
                throw new RuntimeException("Unsupported version: " + version);
            }
        }
    }
}
