package io.github.merykitty.slpprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.merykitty.slpprocessor.common.SLPFiles;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PaletteContainer;

public class Test {
    private static final Path PALETTE_FOLDER = Path.of("/mnt/c/Program Files (x86)/Steam/steamapps/common/AoEDE/Assets/Palettes");
    private static final Path CONFIG_FILE = Path.of(Palette.class.getResource("aoe1-config.json").getPath());
    private static final Path DECODE_INPUT_FOLDER = Path.of("./data/decode-input-test");
    private static final Path DECODE_OUTPUT_FOLDER = Path.of("./data/decode-output-test");
    private static final Path ENCODE_INPUT_FOLDER = Path.of("./data/encode-input");
    private static final Path ENCODE_OUTPUT_FOLDER = Path.of("./data/encode-output");

    public static void main(String[] args) throws IOException {
        decode();
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
            System.out.println("File name: " + fileName.toString());
            System.out.println("Size: " + Files.size(path));
            long start = System.currentTimeMillis();
            var file = SLPFiles.decode(path, palettes);
            long mid = System.currentTimeMillis();
            System.out.println("Read slp file: " + (mid - start) + " ms");
            file.exportGraphics(DECODE_OUTPUT_FOLDER.resolve(fileName));
            long end = System.currentTimeMillis();
            System.out.println("Print data: " + (end - mid) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (AssertionError e) {
            e.printStackTrace();
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
}
