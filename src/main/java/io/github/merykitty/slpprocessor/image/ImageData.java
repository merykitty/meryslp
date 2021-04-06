package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.Image;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@__primitive__
public class ImageData implements AutoCloseable {
    private static final long PIXEL_SIZE = 4;

    private MemorySegment data;

    private ImageData(MemorySegment data) {
        this.data = data;
    }

    public static ImageData createImage(int width, int height) {
        var data = MemorySegment.allocateNative(width * height * PIXEL_SIZE);
        return new ImageData(data);
    }

    public static ImageData createImage(MemorySegment data) {
        return new ImageData(data);
    }

    public static Image importImage(Path path) throws IOException {
        return PNGProcessor.pngRead(path);
    }

    public void exportImage(Path path, int width, int height) throws IOException {
        PNGProcessor.pngWrite(path, new Image(width, height, this.data));
    }

    public void writePixel(int x, int y, RawColour colour, int width, int height) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        MemoryAccess.setIntAtOffset(this.data, (y * width + x) * PIXEL_SIZE, colour.toRGBA());
    }

    public RawColour readPixel(int x, int y, int width, int height) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        int argb = MemoryAccess.getIntAtOffset(this.data, (y * width + x) * PIXEL_SIZE);
        return RawColour.fromRGBA(argb);
    }

    @Override
    public void close() {
        this.data.close();
    }
}
