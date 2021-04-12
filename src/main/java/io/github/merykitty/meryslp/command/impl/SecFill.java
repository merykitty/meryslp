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
public class SecFill implements SecondaryCommand {
    private static final int CMD_BYTE_CAPACITY = 1 << 4;
    private static final int COMMAND_CAPACITY = 1 << 8;

    private int length;
    private int commandLength;
    private ubyte shade;

    public SecFill(MemorySegment data, long offset, SecFrameType frameType) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        int length = cmdByte.value() >>> 4;
        int commandLength;
        if (length != 0) {
            commandLength = 1;
        } else {
            commandLength = 2;
            length = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1)).value();
        }
        this.commandLength = commandLength;
        this.length = length;
        this.shade = frameType.value(new ubyte(MemoryAccess.getByteAtOffset(data, offset + commandLength)));
    }

    public SecFill(SecondaryArtboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        var shadeOptional = artboard.readMain(startX, y);
        BooleanSupplier verifier = () -> {
            if (length < Commands.FILL_THRESHOLD) {
                return false;
            }
            if (shadeOptional.isEmpty()) {
                return false;
            }
            ubyte shade = shadeOptional.get();
            for (int x = startX + 1; x < endX; x++) {
                var current = artboard.readMain(x, y);
                if (current.isEmpty() || current.get() != shade) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        this.length = length;
        this.shade = shadeOptional.get();
        if (length < CMD_BYTE_CAPACITY) {
            this.commandLength = 1;
        } else {
            this.commandLength = 2;
        }
    }

    @Override
    public DrawCursor draw(SecondaryArtboard artboard, DrawCursor cursor) {
        int x = cursor.x();
        int y = cursor.y();
        for (int i = 0; i < this.length; i++) {
            artboard.writeMain(x + i, y, this.shade);
        }
        return new DrawCursor(x + this.length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, SecFrameType type) {
        if (this.commandLength == 1) {
            MemoryAccess.setByteAtOffset(data, offset, (byte)((this.length << 4) | 0x07));
        } else {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(0x07));
            MemoryAccess.setByteAtOffset(data, offset + 1, (byte)this.length);
        }
        MemoryAccess.setByteAtOffset(data, offset + 2, type.reverse(shade).signed());
    }

    @Override
    public long commandLength() {
        return this.commandLength;
    }

    @Override
    public long dataLength() {
        return 1;
    }
}
