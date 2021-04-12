package io.github.merykitty.meryslp.command.impl;

import io.github.merykitty.meryslp.command.Command;
import io.github.merykitty.meryslp.command.Commands;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.image.*;
import io.github.merykitty.meryslp.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class Fill implements Command {
    private static final int CMD_BYTE_CAPACITY = 1 << 4;
    private static final int COMMAND_CAPACITY = 1 << 8;

    private PxRawColour colour;
    private int length;
    private int commandLength;

    public Fill(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
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
        this.colour = PxRawColour.ofNativeData(data, offset + commandLength, dataType, palette);
    }

    public Fill(Artboard artboard, int startX, int endX, int y) {
        var rawOptional = artboard.readRaw(startX, y);
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length < Commands.FILL_THRESHOLD) {
                return false;
            }
            if (rawOptional.isEmpty()) {
                return false;
            }
            var colour = rawOptional.get();
            for (int x = startX + 1; x < endX; x++) {
                var current = artboard.readRaw(x, y);
                if (current.isEmpty() || current.get() != colour) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        this.length = length;
        this.colour = PxRawColour.of(rawOptional.get());
        if (length < CMD_BYTE_CAPACITY) {
            this.commandLength = 1;
        } else {
            this.commandLength = 2;
        }
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        int x = cursor.x();
        int y = cursor.y();
        for (int i = 0; i < this.length; i++) {
            artboard.writeRaw(x + i, y, this.colour.colour());
        }
        return new DrawCursor(x + this.length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        if (this.commandLength == 1) {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(this.length << 4 | 0x07));
        } else {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(0x07));
            MemoryAccess.setByteAtOffset(data, offset + 1, (byte)this.length);
        }
        this.colour.toNative(data, offset + this.commandLength, dataType, palette);
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
