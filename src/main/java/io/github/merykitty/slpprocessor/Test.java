package io.github.merykitty.slpprocessor;

import io.github.merykitty.slpprocessor.command.impl.Fill;
import io.github.merykitty.slpprocessor.common.SLPFile;
import io.github.merykitty.slpprocessor.common.SLPFiles;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PaletteContainer;
import io.github.merykitty.slpprocessor.slpv4.SLPFileVer4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Test {
    private static final Path SLP_FOLDER = Path.of("/mnt/e/stuffs/aoemod/slp"); //Path.of("/mnt/c/Program Files (x86)/Steam/steamapps/common/AoEDE/Assets/SLP");
    private static final Path PALETTE_FOLDER = Path.of("/mnt/c/Program Files (x86)/Steam/steamapps/common/AoEDE/Assets/Palettes");
    private static final Path CONFIG_FILE = Path.of(Palette.class.getResource("aoe1-config.json").getPath());
    private static final Path OUTPUT_FOLDER = Path.of("/mnt/e/stuffs/aoemod/modified");
    private static final Path REVERSE_FOLDER = Path.of("/mnt/e/stuffs/aoemod/slp");

    public static void main(String[] args) throws IOException {
        encode();
    }

    private static void decode() throws IOException {
        var palettes = new PaletteContainer(CONFIG_FILE, PALETTE_FOLDER);
        var files = Files.list(SLP_FOLDER);
        files.filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().contains("tree"))
                .forEach(path -> processSLPFile(path, palettes));
    }

    private static void processSLPFile(Path path, PaletteContainer palettes) {
        try {
            path = path.getFileName();
            assert(!path.isAbsolute());
            System.out.println("File name: " + path.toString());
            System.out.println("Size: " + Files.size(SLP_FOLDER.resolve(path)));
            long start = System.currentTimeMillis();
            var file = SLPFiles.decode(SLP_FOLDER.resolve(path), palettes);
            long mid = System.currentTimeMillis();
            System.out.println("Read slp file: " + (mid - start) + " ms");
            file.exportGraphics(OUTPUT_FOLDER.resolve(path));
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
        var folders = Files.list(OUTPUT_FOLDER);
        folders.forEach(path -> {
            encodeSLP(path, palettes);
        });
    }

    private static void encodeSLP(Path path, PaletteContainer palettes) {
        try {
            path = path.getFileName();
            assert(!path.isAbsolute());
            System.out.println("File name: " + path.toString());
            long start = System.currentTimeMillis();
            var file = SLPFiles.importGraphics(OUTPUT_FOLDER.resolve(path), palettes);
            long mid = System.currentTimeMillis();
            System.out.println("Import graphics: " + (mid - start) + " ms");
            SLPFiles.encode(REVERSE_FOLDER.resolve(path), palettes, file, true);
            long end = System.currentTimeMillis();
            System.out.println("Encode slp file: " + (end - mid) + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
