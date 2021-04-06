package io.github.merykitty.slpprocessor.command.impl;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.Commands;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.image.Artboard;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PxColourValueType;
import io.github.merykitty.slpprocessor.image.PxPlayerColour;
import io.github.merykitty.slpprocessor.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class FillPlayerColour implements Command {
    private static final int CMD_BYTE_CAPACITY = 1 << 4;
    private static final int COMMAND_CAPACITY = 1 << 8;

    private PxPlayerColour colour;
    private int length;
    private int commandLength;

    public FillPlayerColour(MemorySegment data, long offset, PxColourValueType dataType) {
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
        this.colour = PxPlayerColour.ofNativeData(data, offset + commandLength, dataType);
    }

    public FillPlayerColour(Artboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        var shadeOptional = artboard.readPlayer(startX, y);
        BooleanSupplier verifier = () -> {
            if (length < Commands.FILL_THRESHOLD) {
                return false;
            }
            if (shadeOptional.isEmpty()) {
                return false;
            }
            ubyte shade = shadeOptional.get();
            for (int x = startX + 1; x < endX; x++) {
                var current = artboard.readPlayer(x, y);
                if (current.isEmpty() || current.get() != shade) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        this.length = length;
        this.colour = PxPlayerColour.of(shadeOptional.get());
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
            artboard.writePlayer(x + i, y, this.colour.shade());
        }
        return new DrawCursor(x + this.length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        if (this.commandLength == 1) {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(this.length << 4 | 0x0a));
        } else {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(0x0a));
            MemoryAccess.setByteAtOffset(data, offset + 1, (byte)this.length);
        }
        this.colour.toNative(data, offset + this.commandLength, dataType);
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
