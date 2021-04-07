package io.github.merykitty.slpprocessor;

import io.github.merykitty.slpprocessor.common.SLPFiles;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PaletteContainer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.nio.file.Files;
import java.nio.file.Path;

public class SLPEncoder {
    private static final Path CONFIG_FILE = Path.of(Palette.class.getResource("aoe1-config.json").getPath());
    private static final Path PALETTE_FOLDER = Path.of("Assets/Palettes");

    public static void main(String[] args) {
        var parser = ArgumentParsers.newFor("SLPDecoder").build()
                .defaultHelp(true)
                .description("Encode human readable graphics images and meta data to SLP files");
        parser.addArgument("-i", "--input")
                .help("The folder in which graphics files are located");
        parser.addArgument("-o", "--output")
                .help("The folder in which output slp files will be stored");
        parser.addArgument("-h", "--home")
                .help("The home folder of AoE DE, often under Steam/steamapps/common/AoEDE");
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.out.println("Incorrect command line format");
            return;
        }
        var paletteFolder = Path.of(ns.getString("home")).resolve(PALETTE_FOLDER);
        var inputFolder = Path.of(ns.getString("input"));
        var outputFolder = Path.of(ns.getString("output"));
        try {
            var palettes = new PaletteContainer(CONFIG_FILE, paletteFolder);
            var files = Files.list(inputFolder);
            files.filter(Files::isDirectory)
                    .forEach(path -> encodeSLP(path, outputFolder, palettes));
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getCause().toString());
        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    private static void encodeSLP(Path inputFile, Path outputFolder, PaletteContainer palettes) {
        try {
            var fileName = inputFile.getFileName();
            System.out.println("File name: " + fileName.toString());
            long start = System.currentTimeMillis();
            var file = SLPFiles.importGraphics(inputFile, palettes);
            long mid = System.currentTimeMillis();
            System.out.println("Import graphics: " + (mid - start) + " ms");
            SLPFiles.encode(outputFolder.resolve(inputFile), palettes, file, true);
            long end = System.currentTimeMillis();
            System.out.println("Encode slp file: " + (end - mid) + " ms");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
