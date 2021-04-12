package io.github.merykitty.meryslp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import io.github.merykitty.meryslp.common.SLPFiles;
import io.github.merykitty.meryslp.image.PaletteContainer;
import io.github.merykitty.meryslp.misc.EnvironmentResolver;

public class Test {
    private static final Path HOME_DIR = EnvironmentResolver.homeDir();
    private static final Path PALETTE_FOLDER = HOME_DIR.resolve("resources/palettes");
    private static final Path CONFIG_FILE = HOME_DIR.resolve("resources/aoe1-config.json");
    private static final Path DECODE_INPUT_FOLDER = HOME_DIR.resolve("data/decoder-input");
    private static final Path DECODE_OUTPUT_FOLDER = HOME_DIR.resolve("data/decoder-output");
    private static final Path ENCODE_INPUT_FOLDER = HOME_DIR.resolve("data/encoder-input");
    private static final Path ENCODE_OUTPUT_FOLDER = HOME_DIR.resolve("data/encoder-output");

    public static void main(String[] args) throws IOException {
        EnvironmentResolver.setLogLevel(Level.ALL);
//        var palettes = new PaletteContainer(CONFIG_FILE, PALETTE_FOLDER);
//        var files = Files.list(DECODE_INPUT_FOLDER);
//        files.filter(Files::isRegularFile)
//                .forEach(path -> selfDecode(path, palettes));
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            decode();
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    private static void decode() throws IOException {
        var palettes = new PaletteContainer(CONFIG_FILE, PALETTE_FOLDER);
        var files = Files.list(DECODE_INPUT_FOLDER);
        files.filter(Files::isRegularFile)
                .forEach(path -> processSLPFile(path, palettes));
    }

    private static void processSLPFile(Path path, PaletteContainer palettes) {
        try {
            var fileName = path.getFileName();
            long start = System.currentTimeMillis();
            var file = SLPFiles.decode(path, palettes);
            long mid = System.currentTimeMillis();
            file.exportGraphics(DECODE_OUTPUT_FOLDER.resolve(fileName));
            long end = System.currentTimeMillis();
        } catch (IOException e) {
//            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
//            e.printStackTrace();
        } catch (AssertionError e) {
//            e.printStackTrace();
        }
    }

    private static void encode() throws IOException {
        var palettes = new PaletteContainer(CONFIG_FILE, PALETTE_FOLDER);
        var folders = Files.list(ENCODE_INPUT_FOLDER);
        folders.forEach(path -> {
            encodeSLP(path, palettes);
        });
    }

    private static void encodeSLP(Path path, PaletteContainer palettes) {
        try {
            var fileName = path.getFileName();
            System.out.println("File name: " + fileName.toString());
            long start = System.currentTimeMillis();
            var file = SLPFiles.importGraphics(path, palettes);
            long mid = System.currentTimeMillis();
            System.out.println("Import graphics: " + (mid - start) + " ms");
            SLPFiles.encode(ENCODE_OUTPUT_FOLDER.resolve(fileName), palettes, file, true);
            long end = System.currentTimeMillis();
            System.out.println("Encode slp file: " + (end - mid) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void selfDecode(Path path, PaletteContainer palettes) {
        try {
            var fileName = path.getFileName();
            System.out.println("File name: " + fileName.toString());
            long start = System.currentTimeMillis();
            var file = SLPFiles.decode(path, palettes);
            SLPFiles.encode(ENCODE_OUTPUT_FOLDER.resolve(fileName), palettes, file, true);
            long end = System.currentTimeMillis();
            System.out.println("Time: " + (end - start) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
