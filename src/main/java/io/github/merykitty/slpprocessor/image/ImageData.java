package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.Image;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;
import sun.misc.Unsafe;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@__primitive__
public class ImageData implements AutoCloseable {
    private static final long PIXEL_SIZE = 4;
    private static final Unsafe UNSAFE;

    static {
        try {
            var unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            UNSAFE = (Unsafe)unsafeField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MemorySegment data;
    private long dataAddress;

    private ImageData(MemorySegment data) {
        this.data = data;
        this.dataAddress = data.address().toRawLongValue();
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
        UNSAFE.putInt(dataAddress + (y * width + x) * PIXEL_SIZE, colour.toRGBA());
//        MemoryAccess.setIntAtIndex(this.data, y * width + x, colour.toRGBA());
    }

    public RawColour readPixel(int x, int y, int width, int height) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        int rgba = UNSAFE.getInt(this.dataAddress + (y * width + x) * PIXEL_SIZE);
//        int rgba = MemoryAccess.getIntAtIndex(this.data, y * width + x);
        return RawColour.fromRGBA(rgba);
    }

    @Override
    public void close() {
        this.data.close();
    }
}
