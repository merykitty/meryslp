package io.github.merykitty.slpprocessor.command.impl;

import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.command.SecondaryCommand;
import io.github.merykitty.slpprocessor.image.SecFrameType;
import io.github.merykitty.slpprocessor.image.SecondaryArtboard;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

@__primitive__
public class SecEndOfRow implements SecondaryCommand {
    public SecEndOfRow() {}

    @Override
    public DrawCursor draw(SecondaryArtboard artboard, DrawCursor cursor) {
        return cursor;
    }

    @Override
    public void toNative(MemorySegment data, long offset, SecFrameType type) {
        MemoryAccess.setByteAtOffset(data, offset, (byte)0x0f);
    }

    @Override
    public long commandLength() {
        return 1;
    }

    @Override
    public long dataLength() {
        return 0;
    }
}
