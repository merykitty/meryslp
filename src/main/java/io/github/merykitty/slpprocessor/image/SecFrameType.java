package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.ubyte;

public enum SecFrameType {
    SHADOW,
    VFX_ALPHA;

    public ubyte value(ubyte input) {
        return input;
    }

    public ubyte reverse(ubyte input) {
        return input;
    }
}
