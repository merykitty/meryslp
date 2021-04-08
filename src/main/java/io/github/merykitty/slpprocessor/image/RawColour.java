package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.*;

import java.nio.ByteOrder;

@__primitive__
public class RawColour {
    private ubyte red;
    private ubyte green;
    private ubyte blue;
    private ubyte alpha;

    public RawColour(ubyte red, ubyte green, ubyte blue, ubyte alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public RawColour(ubyte red, ubyte green, ubyte blue) {
        this(red, green, blue, ubyte.MAX_VALUE);
    }

    public static RawColour fromRGBA(int rgbaValue) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            ubyte red = new ubyte((byte) (rgbaValue >>> 24));
            ubyte green = new ubyte((byte) (rgbaValue >>> 16));
            ubyte blue = new ubyte((byte) (rgbaValue >>> 8));
            ubyte alpha = new ubyte((byte) (rgbaValue));
            return new RawColour(red, green, blue, alpha);
        } else {
            ubyte red = new ubyte((byte) (rgbaValue));
            ubyte green = new ubyte((byte) (rgbaValue >>> 8));
            ubyte blue = new ubyte((byte) (rgbaValue >>> 16));
            ubyte alpha = new ubyte((byte) (rgbaValue >>> 24));
            return new RawColour(red, green, blue, alpha);
        }
    }

    public int toRGBA() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return (red.value() << 24) | (green.value() << 16) | (blue.value() << 8) | alpha.value();
        } else {
            return red.value() | (green.value() << 8) | (blue.value() << 16) | (alpha.value() << 24);
        }
    }

    public static RawColour fromBGRA(int bgraValue) {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            ubyte blue = new ubyte((byte) (bgraValue >>> 24));
            ubyte green = new ubyte((byte) (bgraValue >>> 16));
            ubyte red = new ubyte((byte) (bgraValue >>> 8));
            ubyte alpha = new ubyte((byte) (bgraValue));
            return new RawColour(red, green, blue, alpha);
        } else {
            ubyte blue = new ubyte((byte) (bgraValue));
            ubyte green = new ubyte((byte) (bgraValue >>> 8));
            ubyte red = new ubyte((byte) (bgraValue >>> 16));
            ubyte alpha = new ubyte((byte) (bgraValue >>> 24));
            return new RawColour(red, green, blue, alpha);
        }
    }

    public int toBGRA() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            return (blue.value() << 24) | (green.value() << 16) | (red.value() << 8) | alpha.value();
        } else {
            return blue.value() | (green.value() << 8) | (red.value() << 16) | (alpha.value() << 24);
        }
    }

    public static RawColour fromGrey(ubyte shade) {
        return new RawColour(shade, shade, shade);
    }

    public ubyte toGrey() {
        return new ubyte((byte)((this.red.value() + this.green.value() + this.blue.value()) / 3)) ;
    }

    public RawColour reverseAlpha() {
        return new RawColour(this.red, this.green, this.blue, new ubyte((byte)(255 - this.alpha.value())));
    }

    public static int squaredDistance(RawColour c1, RawColour c2) {
        int redDiff = c1.red.value() - c2.red.value();
        int greenDiff = c1.green.value() - c2.green.value();
        int blueDiff = c1.blue.value() - c2.blue.value();
        int alphaDiff = c1.alpha.value() - c2.alpha.value();
        return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff + alphaDiff * alphaDiff;
    }

    public ubyte red() {
        return this.red;
    }

    public ubyte green() {
        return this.green;
    }

    public ubyte blue() {
        return this.blue;
    }

    public ubyte alpha() {
        return this.alpha;
    }
}
