package io.github.merykitty.slpprocessor.common;

import io.github.merykitty.slpprocessor.image.PaletteContainer;
import jdk.incubator.foreign.MemorySegment;

import java.io.IOException;
import java.nio.file.Path;

public interface SLPFile {
    long VERSION_OFFSET = 0;
    long VERSION_SIZE = 4;

    void exportGraphics(Path exportFolder) throws IOException;

    MemorySegment toNativeData(PaletteContainer palettes);
}
