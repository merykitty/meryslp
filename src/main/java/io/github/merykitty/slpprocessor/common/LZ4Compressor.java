package io.github.merykitty.slpprocessor.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static io.github.merykitty.slpprocessor.misc.EnvironmentResolver.*;
import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;

public class LZ4Compressor {
    private static final MethodHandle COMPRESS_HANDLE;
    private static final MethodHandle DECOMPRESS_HANDLE;
    private static final MethodHandle COMPRESS_BOUND_HANDLE;

    static {
        try {
            LibraryLookup lib;
            if (osName().contains("linux")) {
                lib = LibraryLookup.ofPath(homeDir().resolve("resources/liblz4.so"));
            } else {
                lib = LibraryLookup.ofPath(homeDir().resolve("resources/liblz4.dll"));
            }
            var compressSymbol = lib.lookup("LZ4_compress_default").get();
            var decompressSymbol = lib.lookup("LZ4_decompress_safe").get();
            var compressBoundSymbol = lib.lookup("LZ4_compressBound").get();
            COMPRESS_HANDLE = CLinker.getInstance().downcallHandle(compressSymbol,
                    MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class),
                    FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_INT).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true)
            );
            DECOMPRESS_HANDLE = CLinker.getInstance().downcallHandle(decompressSymbol,
                    MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class, int.class),
                    FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT, C_INT).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true)
            );
            COMPRESS_BOUND_HANDLE = CLinker.getInstance().downcallHandle(compressBoundSymbol,
                    MethodType.methodType(int.class, int.class),
                    FunctionDescriptor.of(C_INT, C_INT).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true)
            );
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment compress(MemorySegment src) {
        try {
            int srcSize = (int)src.byteSize();
            int destSizeBound = (int) COMPRESS_BOUND_HANDLE.invokeExact(srcSize);
            var dest = MemorySegment.allocateNative(destSizeBound);
            int destSize = (int) COMPRESS_HANDLE.invokeExact(src.address(), dest.address(), srcSize, destSizeBound);
            assert(destSize > 0 && destSize <= destSizeBound);
            return dest.asSlice(0, destSize);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment decompress(MemorySegment src, long expectedDestSize) {
        try {
            var dest = MemorySegment.allocateNative(expectedDestSize);
            int actualDestSize = (int) DECOMPRESS_HANDLE.invokeExact(src.address(), dest.address(), (int) src.byteSize(), (int) expectedDestSize);
            assert(actualDestSize == expectedDestSize);
            return dest.withAccessModes(dest.accessModes() & ~MemorySegment.WRITE);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
