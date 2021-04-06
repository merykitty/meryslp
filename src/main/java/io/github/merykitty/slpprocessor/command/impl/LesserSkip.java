package io.github.merykitty.slpprocessor.command.impl;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.Commands;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.image.Artboard;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PxColourValueType;
import io.github.merykitty.slpprocessor.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class LesserSkip implements Command {
    private static final long COMMAND_LENGTH = 1;

    private int length;

    public LesserSkip(MemorySegment data, long offset) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        this.length = cmdByte.value() >>> 2;
    }

    public LesserSkip(Artboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length >= Commands.LESSER_THRESHOLD) {
                return false;
            }
            for (int x = startX; x < endX; x++) {
                if (artboard.readRaw(x, y).isPresent() || artboard.readOutline(x, y).isPresent() || artboard.readPlayer(x, y).isPresent()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean());
        this.length = length;
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        return new DrawCursor(cursor.x() + this.length, cursor.y());
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
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
