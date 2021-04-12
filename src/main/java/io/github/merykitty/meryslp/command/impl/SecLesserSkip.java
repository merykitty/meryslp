package io.github.merykitty.meryslp.command.impl;

import io.github.merykitty.meryslp.command.Commands;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.command.SecondaryCommand;
import io.github.merykitty.meryslp.image.SecFrameType;
import io.github.merykitty.meryslp.image.SecondaryArtboard;
import io.github.merykitty.meryslp.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class SecLesserSkip implements SecondaryCommand {
    private static final long COMMAND_LENGTH = 1;

    private int length;

    public SecLesserSkip(MemorySegment data, long offset) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        this.length = cmdByte.value() >>> 2;
    }

    public SecLesserSkip(SecondaryArtboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length >= Commands.LESSER_THRESHOLD) {
                return false;
            }
            for (int x = startX; x < endX; x++) {
                if (artboard.readMain(x, y).isPresent()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean());
        this.length = length;
    }

    @Override
    public DrawCursor draw(SecondaryArtboard artboard, DrawCursor cursor) {
        return new DrawCursor(cursor.x() + this.length, cursor.y());
    }

    @Override
    public void toNative(MemorySegment data, long offset, SecFrameType type) {
        MemoryAccess.setByteAtOffset(data, offset, (byte)((this.length << 2) | 0x01));
    }

    @Override
    public long commandLength() {
        return COMMAND_LENGTH;
    }

    @Override
    public long dataLength() {
        return 0;
    }
}
