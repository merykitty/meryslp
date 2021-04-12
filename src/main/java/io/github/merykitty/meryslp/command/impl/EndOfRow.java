package io.github.merykitty.meryslp.command.impl;

import io.github.merykitty.meryslp.command.Command;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.image.Artboard;
import io.github.merykitty.meryslp.image.Palette;
import io.github.merykitty.meryslp.image.PxColourValueType;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

@__primitive__
public class EndOfRow implements Command {
    @Override
    public long commandLength() {
        return 1;
    }

    @Override
    public long dataLength() {
        return 0;
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        return cursor;
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        MemoryAccess.setByteAtOffset(data, offset, (byte)0x0f);
    }
}
