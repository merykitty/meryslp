package io.github.merykitty.slpprocessor.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HomeDirectoryResolver {
    private static final Path HOME_DIR;

    static {
        try {
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("linux")) {
                var executable = Files.readSymbolicLink(Path.of("/proc/self/exe"));
                var fileName = executable.getFileName();
                if (fileName.toString().equals("java")) {
                    HOME_DIR = Path.of(System.getenv("HOME_DIR")).toAbsolutePath();
                } else {
                    HOME_DIR = executable.getParent().getParent().toAbsolutePath();
                }
            } else {
                throw new RuntimeException("Unsupported os: " + osName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path homeDir() {
        return HOME_DIR;
    }
}
