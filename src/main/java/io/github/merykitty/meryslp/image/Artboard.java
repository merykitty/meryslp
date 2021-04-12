package io.github.merykitty.meryslp.image;

import io.github.merykitty.meryslp.misc.PrimitiveOptionalRawColour;
import io.github.merykitty.meryslp.misc.PrimitiveOptionalUbyte;
import io.github.merykitty.meryslp.misc.ubyte;

import java.io.IOException;
import java.nio.file.Path;

@__primitive__
public class Artboard implements AutoCloseable {
    private static final String RAW_CHANNEL = "raw";
    private static final String PLAYER_CHANNEL = "player";
    private static final String OUTLINE_CHANNEL = "outline";

    private int width;
    private int height;
    private ImageData rawChannel;
    private ImageData outlineChannel;
    private ImageData playerChannel;

    private Artboard(int width, int height, ImageData rawChannel, ImageData outlineChannel, ImageData playerChannel) {
        this.width = width;
        this.height = height;
        this.rawChannel = rawChannel;
        this.outlineChannel = outlineChannel;
        this.playerChannel = playerChannel;
    }

    public static Artboard createFrame(int width, int height) {
        var rawChannel = ImageData.createImage(width, height);
        var outlineChannel = ImageData.createImage(width, height);
        var playerChannel = ImageData.createImage(width, height);
        return new Artboard(width, height, rawChannel, outlineChannel, playerChannel);
    }

    public static Artboard importFrame(Path folder, int frameNum) throws IOException {
        var rawImage = ImageData.importImage(folder.resolve(frameNum + "_" + RAW_CHANNEL + ".png"));
        var outLineImage = ImageData.importImage(folder.resolve(frameNum + "_" + OUTLINE_CHANNEL + ".png"));
        var playerImage = ImageData.importImage(folder.resolve(frameNum + "_" + PLAYER_CHANNEL + ".png"));
        int width = rawImage.width();
        int height = rawImage.height();
        var rawChannel = ImageData.createImage(rawImage.data());
        assert(outLineImage.width() == width && outLineImage.height() == height);
        var outlineChannel = ImageData.createImage(outLineImage.data());
        assert(playerImage.width() == width && playerImage.height() == height);
        var playerChannel = ImageData.createImage(playerImage.data());
        return new Artboard(width, height, rawChannel, outlineChannel, playerChannel);
    }

    public void exportFrame(Path folder, int frameNum) throws IOException {
        this.rawChannel.exportImage(folder.resolve(frameNum + "_" + RAW_CHANNEL + ".png"), this.width, this.height);
        this.outlineChannel.exportImage(folder.resolve(frameNum + "_" + OUTLINE_CHANNEL + ".png"), this.width, this.height);
        this.playerChannel.exportImage(folder.resolve(frameNum + "_" + PLAYER_CHANNEL + ".png"), this.width, this.height);
    }

    public void writeRaw(int x, int y, RawColour colour) {
        this.rawChannel.writePixel(x, y, colour, this.width, this.height);
    }

    public void writeOutline(int x, int y, ubyte shade) {
        this.outlineChannel.writePixel(x, y, RawColour.fromGrey(shade), this.width, this.height);
    }

    public void writePlayer(int x, int y, ubyte shade) {
        this.playerChannel.writePixel(x, y, RawColour.fromGrey(shade), this.width, this.height);
    }

    public PrimitiveOptionalRawColour readRaw(int x, int y) {
        var colour = this.rawChannel.readPixel(x, y, this.width, this.height);
        if (colour.alpha() == ubyte.default) {
            return PrimitiveOptionalRawColour.empty();
        } else {
            return PrimitiveOptionalRawColour.of(colour);
        }
    }

    public PrimitiveOptionalUbyte readOutline(int x, int y) {
        var colour = this.outlineChannel.readPixel(x, y, this.width, this.height);
        if (colour.alpha() == ubyte.default) {
            return PrimitiveOptionalUbyte.empty();
        } else {
            return PrimitiveOptionalUbyte.of(colour.toGrey());
        }
    }

    public PrimitiveOptionalUbyte readPlayer(int x, int y) {
        var colour = this.playerChannel.readPixel(x, y, this.width, this.height);
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
        try(var raw = this.rawChannel; var outline = this.outlineChannel; var player = this.playerChannel;) {}
    }

}
