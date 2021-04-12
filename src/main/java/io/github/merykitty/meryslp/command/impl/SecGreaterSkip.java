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
public class SecGreaterSkip implements SecondaryCommand {
    private static final long COMMAND_LENGTH = 2;
    private static final int COMMAND_CAPACITY = 1 << 12;

    private int length;

    public SecGreaterSkip(MemorySegment data, long offset) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        ubyte nextByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1));
        this.length = ((cmdByte.value() & 0xf0) << 4) | nextByte.value();
    }

    public SecGreaterSkip(SecondaryArtboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length < Commands.LESSER_THRESHOLD) {
                return false;
            }
            for (int x = startX; x < endX; x++) {
                if (artboard.readMain(x, y).isPresent()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        this.length = length;
    }

    @Override
    public DrawCursor draw(SecondaryArtboard artboard, DrawCursor cursor) {
        return new DrawCursor(cursor.x() + this.length, cursor.y());
    }

    @Override
    public void toNative(MemorySegment data, long offset, SecFrameType type) {
        MemoryAccess.setByteAtOffset(data, offset, (byte)(((this.length >>> 4) & 0xf0) | 0x03));
        MemoryAccess.setByteAtOffset(data, offset + 1, (byte)this.length);
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
