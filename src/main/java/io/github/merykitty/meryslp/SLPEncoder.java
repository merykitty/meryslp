package io.github.merykitty.meryslp;

import io.github.merykitty.meryslp.common.SLPFiles;
import io.github.merykitty.meryslp.image.PaletteContainer;
import io.github.merykitty.meryslp.misc.EnvironmentResolver;
import io.github.merykitty.meryslp.misc.PrimitiveOptional;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SLPEncoder {
    private static final Path DEFAULT_CONFIG_FILE = Path.of("resources/aoe1-config.json");
    private static final Path DEFAULT_PALETTE_FOLDER = Path.of("resources/palettes");
    private static final Path DEFAULT_INPUT_FOLDER = Path.of("data/encoder-input");
    private static final Path DEFAULT_OUTPUT_FOLDER = Path.of("data/encoder-output");

    public static void main(String[] args) throws IOException {
        var parser = ArgumentParsers.newFor("SLPDecoder").build()
                .defaultHelp(true)
                .description("Encode human readable graphics images and meta data to SLP files");
        parser.addArgument("-i", "--input").required(false)
                .help("The folder in which graphics files are located, default to data/encoder-input under the program folder");
        parser.addArgument("-d", "--output").required(false)
                .help("The folder in which output slp files will be stored, default to data/encoder-output under the program folder");
        parser.addArgument("--palettes").required(false)
                .help("The palettes folder of AoE DE, often under Steam/steamapps/common/AoEDE/Assets/Palettes, default to resources/palettes under the program folder");
        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.out.println("Incorrect command line format");
            return;
        }

        var configFile = EnvironmentResolver.homeDir().resolve(DEFAULT_CONFIG_FILE);
        var paletteFolder = PrimitiveOptional.ofNullable(ns.getString("palettes"))
                .map(Path::of)
                .orElse(EnvironmentResolver.homeDir().resolve(DEFAULT_PALETTE_FOLDER));
        var inputFolder = PrimitiveOptional.ofNullable(ns.getString("input"))
                .map(Path::of)
                .orElse(EnvironmentResolver.homeDir().resolve(DEFAULT_INPUT_FOLDER));
        var outputFolder = PrimitiveOptional.ofNullable(ns.getString("output"))
                .map(Path::of)
                .orElse(EnvironmentResolver.homeDir().resolve(DEFAULT_OUTPUT_FOLDER));

        try {
            var palettes = new PaletteContainer(configFile, paletteFolder);
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
            SLPFiles.encode(outputFolder.resolve(fileName), palettes, file, true);
            long end = System.currentTimeMillis();
            System.out.println("Encode slp file: " + (end - mid) + " ms");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }
}
