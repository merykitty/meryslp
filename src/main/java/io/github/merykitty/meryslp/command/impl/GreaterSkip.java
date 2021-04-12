package io.github.merykitty.meryslp.command.impl;

import io.github.merykitty.meryslp.command.Command;
import io.github.merykitty.meryslp.command.Commands;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.image.Artboard;
import io.github.merykitty.meryslp.image.Palette;
import io.github.merykitty.meryslp.image.PxColourValueType;
import io.github.merykitty.meryslp.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class GreaterSkip implements Command {
    private static final long COMMAND_LENGTH = 2;
    private static final int COMMAND_CAPACITY = 1 << 12;

    private int length;

    public GreaterSkip(MemorySegment data, long offset) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        ubyte nextByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1));
        this.length = ((cmdByte.value() & 0xf0) << 4) | nextByte.value();
    }

    public GreaterSkip(Artboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length < Commands.LESSER_THRESHOLD) {
                return false;
            }
            for (int x = startX; x < endX; x++) {
                if (artboard.readRaw(x, y).isPresent() || artboard.readOutline(x, y).isPresent() || artboard.readPlayer(x, y).isPresent()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        this.length = length;
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        return new DrawCursor(cursor.x() + this.length, cursor.y());
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
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
