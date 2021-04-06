package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.PrimitiveOptionalUbyte;
import io.github.merykitty.slpprocessor.misc.ubyte;

import java.io.IOException;
import java.nio.file.Path;

@__primitive__
public class SecondaryArtboard implements AutoCloseable {
    private static final String MAIN_CHANNEL = "main";

    private int width;
    private int height;
    private ImageData mainChannel;

    private SecondaryArtboard(int width, int height, ImageData mainChannel) {
        this.width = width;
        this.height = height;
        this.mainChannel = mainChannel;
    }

    public static SecondaryArtboard createFrame(int width, int height) {
        var mainChannel = ImageData.createImage(width, height);
        return new SecondaryArtboard(width, height, mainChannel);
    }

    public static SecondaryArtboard importFrame(Path folder, int frameNum) throws IOException {
        var mainImage = ImageData.importImage(folder.resolve(frameNum + "_" + MAIN_CHANNEL + ".png"));
        int width = mainImage.width();
        int height = mainImage.height();
        var mainChannel = ImageData.createImage(mainImage.data());
        return new SecondaryArtboard(width, height, mainChannel);
    }

    public void exportFrame(Path folder, int frameNum) throws IOException {
        this.mainChannel.exportImage(folder.resolve(frameNum + "_" + MAIN_CHANNEL + ".png"), width, height);
    }

    public void writeMain(int x, int y, ubyte shade) {
        this.mainChannel.writePixel(x, y, RawColour.fromGrey(shade), width, height);
    }

    public PrimitiveOptionalUbyte readMain(int x, int y) {
        var colour = this.mainChannel.readPixel(x, y, this.width, this.height);
        if (colour.alpha() == ubyte.default) {
            return PrimitiveOptionalUbyte.empty();
        } else {
            return PrimitiveOptionalUbyte.of(colour.toGrey());
        }
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    @Override
    public void close() {
        try(var main = this.mainChannel;) {}
    }
}
