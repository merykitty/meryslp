package io.github.merykitty.meryslp.command.impl;

import io.github.merykitty.meryslp.command.Command;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.image.Artboard;
import io.github.merykitty.meryslp.image.Palette;
import io.github.merykitty.meryslp.image.PxColourValueType;
import jdk.incubator.foreign.MemorySegment;

@__primitive__
public class ShadowDraw implements Command {
    public ShadowDraw() {
        throw new AssertionError();
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        throw new AssertionError();
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        throw new AssertionError();
    }

    @Override
    public long commandLength() {
        throw new AssertionError();
    }

    @Override
    public long dataLength() {
        throw new AssertionError();
    }

}
