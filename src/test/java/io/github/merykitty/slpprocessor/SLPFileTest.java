package io.github.merykitty.slpprocessor;

import java.io.IOException;
import java.nio.file.Path;

import io.github.merykitty.slpprocessor.common.SLPFiles;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PaletteContainer;
import org.junit.Test;

public class SLPFileTest {
    private static final Path testFile = Path.of("/mnt/c/Program Files (x86)/Steam/steamapps/common/AoEDE/Assets/SLP/b_gre_wonder_destruction_x2.slp");
    private static final String PALETTE_FOLDER = "/mnt/c/Program Files (x86)/Steam/steamapps/common/AoEDE/Assets/Palettes";
    private static final String CONFIG_FILE = Palette.class.getResource("aoe1-config.json").getPath();

    public void testFile() throws IOException {
        var palettes = new PaletteContainer(Path.of(CONFIG_FILE), Path.of(PALETTE_FOLDER));
        SLPFiles.decode(testFile, palettes);
    }
}
