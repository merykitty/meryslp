package io.github.merykitty.meryslp.image;

import io.github.merykitty.meryslp.misc.ubyte;

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
