package io.github.merykitty.slpprocessor.image;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import io.github.merykitty.slpprocessor.misc.Image;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.C_INT;
import static jdk.incubator.foreign.CLinker.C_POINTER;

public class PNGProcessor {
    private static final long SIGNATURE_LENGTH = 8;
    private static final long IHDR_CHUNK_HEADER = 8;
    private static final long INT_SIZE = 4;
    private static final long PIXEL_SIZE = 4;
    private static final long WIDTH_OFFSET = SIGNATURE_LENGTH + IHDR_CHUNK_HEADER;
    private static final long HEIGHT_OFFSET = WIDTH_OFFSET + INT_SIZE;
    private static final long SIGNATURE_BUG_ENDIAN = 0x89504E470D0A1A0AL;

    private static final MethodHandle PNG_READ_HANDLE;
    private static final MethodHandle PNG_WRITE_HANDLE;

    static {
        try {
            var lib = LibraryLookup.ofPath(Path.of("./resources/pngutils.so"));
            var readSymbol = lib.lookup("png_read").get();
            var writeSymbol = lib.lookup("png_write").get();
            PNG_READ_HANDLE = CLinker.getInstance().downcallHandle(readSymbol,
                    MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class),
                    FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_POINTER).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true)
            );
            PNG_WRITE_HANDLE = CLinker.getInstance().downcallHandle(writeSymbol,
                    MethodType.methodType(int.class, MemoryAddress.class, int.class, int.class, MemoryAddress.class),
                    FunctionDescriptor.of(C_INT, C_POINTER, C_INT, C_INT, C_POINTER).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Image pngRead(Path path) throws IOException {
        var pathString = path.toString();
        try (var meta = MemorySegment.mapFile(path, 0, HEIGHT_OFFSET + INT_SIZE, FileChannel.MapMode.READ_ONLY);
                var cPath = MemorySegment.allocateNative(pathString.length() + 1)) {
            cPath.copyFrom(MemorySegment.ofArray(pathString.getBytes(StandardCharsets.US_ASCII)));
            long signature = MemoryAccess.getLongAtOffset(meta, 0, ByteOrder.BIG_ENDIAN);
            assert(signature == SIGNATURE_BUG_ENDIAN);
            int width = MemoryAccess.getIntAtOffset(meta, WIDTH_OFFSET, ByteOrder.BIG_ENDIAN);
            int height = MemoryAccess.getIntAtOffset(meta, HEIGHT_OFFSET, ByteOrder.BIG_ENDIAN);
            var data = MemorySegment.allocateNative(width * height * PIXEL_SIZE);
            int result = (int)PNG_READ_HANDLE.invokeExact(cPath.address(), width, height, data.address());
            assert(result == 0);
            return new Image(width, height, data);
        } catch (Throwable e) {
            if (e instanceof IOException i) {
                throw i;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public static void pngWrite(Path path, Image image) {
        var pathString = path.toString();
        try (var cPath = MemorySegment.allocateNative(pathString.length() + 1)) {
            cPath.copyFrom(MemorySegment.ofArray(pathString.getBytes(StandardCharsets.US_ASCII)));
            int result = (int)PNG_WRITE_HANDLE.invokeExact(cPath.address(), image.width(), image.height(), image.data().address());
            assert(result == 0);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}