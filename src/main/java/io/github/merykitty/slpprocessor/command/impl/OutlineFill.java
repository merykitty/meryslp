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
public class OutlineFill implements Command {
    private static int COMMAND_CAPACITY = 1 << 8;

    private int length;
    private PxPlayerColour colour;

    public OutlineFill(MemorySegment data, long offset, PxColourValueType type) {
        this.length = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1)).value();
        this.colour = PxPlayerColour.ofNativeData(data, offset + 2, type);
    }

    public OutlineFill(Artboard artboard, int startX, int endX, int y) {
        var outlineOptional = artboard.readOutline(startX, y);
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length < Commands.FILL_THRESHOLD) {
                return false;
            }
            if (outlineOptional.isEmpty()) {
                return false;
            }
            ubyte shade = outlineOptional.get();
            for (int x = startX + 1; x < endX; x++) {
                var current = artboard.readOutline(x, y);
                if (current.isEmpty() || current.get() != shade) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        this.length = length;
        this.colour = PxPlayerColour.of(outlineOptional.get());
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        int x = cursor.x();
        int y = cursor.y();
        for (int i = 0; i < this.length; i++) {
            artboard.writeOutline(x + i, y, this.colour.shade());
        }
        return new DrawCursor(x + this.length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        MemoryAccess.setByteAtOffset(data, offset, (byte)0x5e);
        MemoryAccess.setByteAtOffset(data, offset + 1, (byte)this.length);
        this.colour.toNative(data, offset + 2, dataType);
    }

    @Override
    public long commandLength() {
        return 2;
    }

    @Override
    public long dataLength() {
        return 1;
    }
}
