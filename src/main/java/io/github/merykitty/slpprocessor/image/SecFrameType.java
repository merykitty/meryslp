package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.ubyte;

public enum SecFrameType {
    SHADOW,
    VFX_ALPHA;

    public ubyte value(ubyte input) {
        return switch (this) {
            case SHADOW -> new ubyte((byte) (~(input.value() << 2)));
            case VFX_ALPHA -> input;
        };
    }

    public ubyte reverse(ubyte input) {
        return switch (this) {
            case SHADOW -> new ubyte((byte)((~input.value() & 0xff) >>> 2));
            case VFX_ALPHA -> input;
        };
    }
}
