package io.github.merykitty.slpprocessor;

import java.nio.file.Files;
import java.nio.file.Path;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import io.github.merykitty.slpprocessor.common.SLPFiles;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PaletteContainer;

public class SLPDecoder {
    private static final Path CONFIG_FILE = Path.of("./resources/aoe1-config.json");
    private static final Path PALETTE_FOLDER = Path.of("Assets/Palettes");

    public static void main(String[] args) {
        var parser = ArgumentParsers.newFor("SLPDecoder").build()
                .defaultHelp(true)
                .description("Decode SLP files into human readable graphics images and meta data");
        parser.addArgument("-i", "--input")
                .help("The folder in which slp files are extracted");
        parser.addArgument("-o", "--output")
                .help("The folder in which output files will be stored");
        parser.addArgument("-r", "--root")
                .help("The root directory of AoE DE, often under Steam/steamapps/common/AoEDE");
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.out.println("Incorrect command line format");
            return;
        }
        var paletteFolder = Path.of(ns.getString("root")).resolve(PALETTE_FOLDER);
        var inputFolder = Path.of(ns.getString("input"));
        var outputFolder = Path.of(ns.getString("output"));
        try {
            var palettes = new PaletteContainer(CONFIG_FILE, paletteFolder);
            var files = Files.list(inputFolder);
            files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".slp"))
                    .forEach(path -> {
                        processSLPFile(path, outputFolder, palettes);
                    });
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getCause().toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    private static void processSLPFile(Path inputFile, Path outputFolder, PaletteContainer palettes) {
        try {
            var fileName = inputFile.getFileName();
            System.out.println("File name: " + fileName.toString());
            long start = System.currentTimeMillis();
            var file = SLPFiles.decode(inputFile, palettes);
            long mid = System.currentTimeMillis();
            System.out.println("Read slp file: " + (mid - start) + " ms");
            file.exportGraphics(outputFolder.resolve(inputFile));
            long end = System.currentTimeMillis();
            System.out.println("Print data: " + (end - mid) + " ms");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
