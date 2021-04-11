package io.github.merykitty.slpprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.merykitty.slpprocessor.misc.HomeDirectoryResolver;
import io.github.merykitty.slpprocessor.misc.PrimitiveOptional;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import io.github.merykitty.slpprocessor.common.SLPFiles;
import io.github.merykitty.slpprocessor.image.PaletteContainer;

public class SLPDecoder {
    private static final Path DEFAULT_CONFIG_FILE = Path.of("resources/aoe1-config.json");
    private static final Path DEFAULT_PALETTE_FOLDER = Path.of("resources/palettes");
    private static final Path DEFAULT_INPUT_FOLDER = Path.of("data/decoder-input");
    private static final Path DEFAULT_OUTPUT_FOLDER = Path.of("data/decoder-output");

    public static void main(String[] args) throws IOException {
        var parser = ArgumentParsers.newFor("SLPDecoder").build()
                .defaultHelp(true)
                .description("Decode SLP files into human readable graphics images and meta data");
        parser.addArgument("-i", "--input").required(false)
                .help("The folder in which input slp files are located, default to data/decoder-input under the program folder");
        parser.addArgument("-d", "--output").required(false)
                .help("The folder in which output graphics files will be stored, default to data/decoder-output under the program folder");
        parser.addArgument("--palettes").required(false)
                .help("The palettes folder of AoE DE, often under Steam/steamapps/common/AoEDE/Assets/Palettes, default to resources/palettes under the program folder");
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.out.println("Incorrect command line format");
            return;
        }
        var configFile = HomeDirectoryResolver.homeDir().resolve(DEFAULT_CONFIG_FILE);
        var paletteFolder = PrimitiveOptional.ofNullable(ns.getString("palettes"))
                .map(Path::of)
                .orElse(HomeDirectoryResolver.homeDir().resolve(DEFAULT_PALETTE_FOLDER));
        var inputFolder = PrimitiveOptional.ofNullable(ns.getString("input"))
                .map(Path::of)
                .orElse(HomeDirectoryResolver.homeDir().resolve(DEFAULT_INPUT_FOLDER));
        var outputFolder = PrimitiveOptional.ofNullable(ns.getString("output"))
                .map(Path::of)
                .orElse(HomeDirectoryResolver.homeDir().resolve(DEFAULT_OUTPUT_FOLDER));

        try {
            var palettes = new PaletteContainer(configFile, paletteFolder);
            var files = Files.list(inputFolder);
            files.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".slp"))
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
            file.exportGraphics(outputFolder.resolve(fileName));
            long end = System.currentTimeMillis();
            System.out.println("Print data: " + (end - mid) + " ms");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
