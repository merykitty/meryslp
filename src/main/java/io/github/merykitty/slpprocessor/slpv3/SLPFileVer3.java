package io.github.merykitty.slpprocessor.slpv3;

import io.github.merykitty.slpprocessor.image.PaletteContainer;
import jdk.incubator.foreign.MemorySegment;

import io.github.merykitty.slpprocessor.common.SLPFile;

import java.io.IOException;
import java.nio.file.Path;

@__primitive__
public class SLPFileVer3 implements SLPFile {
    public SLPFileVer3(MemorySegment file, PaletteContainer palettes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportGraphics(Path exportFolder) throws IOException {

    }

    @Override
    public MemorySegment toNativeData(PaletteContainer palettes) {
        return null;
    }
}
