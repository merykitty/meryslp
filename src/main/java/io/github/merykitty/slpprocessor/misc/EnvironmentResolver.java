package io.github.merykitty.slpprocessor.misc;

import jdk.incubator.foreign.*;
import static jdk.incubator.foreign.CLinker.*;

import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class EnvironmentResolver {
    private static final long INIT_MAX_PATH_LENGTH = 256;
    private static final long CHAR_SIZE = 2;

    private static final String OS_NAME;
    private static final Path HOME_DIR;

    static {
        try {
            OS_NAME = System.getProperty("os.name").toLowerCase();
            if (OS_NAME.contains("linux")) {
                var execPath = Files.readSymbolicLink(Path.of("/proc/self/exe"));
                var execName = execPath.getFileName().toString();
                if (execName.equals("java")) {
                    HOME_DIR = Path.of(System.getenv("HOME_DIR")).toAbsolutePath();
                } else {
                    HOME_DIR = execPath.getParent().getParent().toAbsolutePath();
                }
            } else if (OS_NAME.contains("windows")) {
                String execPathString;
                var lib = LibraryLookup.ofLibrary("kernel32");
                var getModuleFileNameSymbol = lib.lookup("GetModuleFileNameW").get();
                var getLastErrorSymbol = lib.lookup("GetLastError").get();
                var setEnvironmentVariableSymbol = lib.lookup("SetEnvironmentVariableW").get();
                var getModuleFileNameHandle = CLinker.getInstance().downcallHandle(getModuleFileNameSymbol,
                        MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class, int.class),
                        FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER, C_INT).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true));
                var getLastErrorHandle = CLinker.getInstance().downcallHandle(getLastErrorSymbol,
                        MethodType.methodType(int.class),
                        FunctionDescriptor.of(C_INT).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true));
                var setEnvironmentVariableHandle = CLinker.getInstance().downcallHandle(setEnvironmentVariableSymbol,
                        MethodType.methodType(int.class, MemoryAddress.class, MemoryAddress.class),
                        FunctionDescriptor.of(C_INT, C_POINTER, C_POINTER).withAttribute(FunctionDescriptor.TRIVIAL_ATTRIBUTE_NAME, true));
                long maxPathLength = INIT_MAX_PATH_LENGTH;
                while (true) {
                    try (var home = MemorySegment.allocateNative(maxPathLength * CHAR_SIZE);) {
                        long pathLength = new uint((int)getModuleFileNameHandle.invokeExact(MemoryAddress.NULL, home.address(), (int)maxPathLength)).value();
                        if (pathLength == 0) {
                            int error = (int)getLastErrorHandle.invokeExact();
                            throw new RuntimeException("Error get executable information, error code " + error);
                        } else if (pathLength < maxPathLength) {
                            execPathString = new String(home.asSlice(0, pathLength * CHAR_SIZE).toCharArray());
                            break;
                        } else {
                            maxPathLength *= 2;
                        }
                    }
                }
                var execPath = Path.of(execPathString);
                String execName = execPath.getFileName().toString();
                if (execName.equals("java.exe")) {
                    HOME_DIR = Path.of(System.getenv("HOME_DIR")).toAbsolutePath();
                } else {
                    HOME_DIR = execPath.getParent().toAbsolutePath();
                }

                String pathEnvVar = System.getProperty("PATH");
                pathEnvVar += ";" + HOME_DIR.resolve("resources").toString() + '\0';
                try (var valueStr = MemorySegment.allocateNative(pathEnvVar.length() * CHAR_SIZE);
                     var keyStr = MemorySegment.allocateNative("PATH\0".length() * CHAR_SIZE)) {
                    valueStr.copyFrom(MemorySegment.ofArray(pathEnvVar.toCharArray()));
                    keyStr.copyFrom(MemorySegment.ofArray("PATH\0".toCharArray()));
                    int result = (int)setEnvironmentVariableHandle.invokeExact(keyStr.address(), valueStr.address());
                    if (result == 0) {
                        int error = (int)getLastErrorHandle.invokeExact();
                        throw new RuntimeException("Error set environment variable, error code " + error);
                    }
                }
            } else {
                throw new RuntimeException("Unsupported os: " + OS_NAME);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Path homeDir() {
        return HOME_DIR;
    }

    public static String osName() {
        return OS_NAME;
    }
}
