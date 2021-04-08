package io.github.merykitty.slpprocessor.command.impl;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.Commands;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.image.*;
import io.github.merykitty.slpprocessor.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class PremultipliedAlpha implements Command {
    private static final long COMMAND_LENGTH = 2;
    private static final int COMMAND_CAPACITY = 1 << 8;

    private PxRawColour[] data;

    public PremultipliedAlpha (MemorySegment data, long offset, PxColourValueType type, Palette palette) {
        assert(type == PxColourValueType.RAW_COLOUR);
        int length = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1)).value();
        var commandData = new PxRawColour[length];
        long currentOffset = offset + 2;
        for (int i = 0; i < length; i++) {
            var colour = RawColour.fromBGRA(MemoryAccess.getIntAtOffset(data, currentOffset)).reverseAlpha();
            commandData[i] = PxRawColour.of(colour);
            assert(colour.alpha().value() < 255);
            currentOffset += 4;
        }
        this.data = commandData;
    }

    public PremultipliedAlpha(Artboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            for (int x = startX; x < endX; x++) {
                var rawOptional = artboard.readRaw(x, y);
                if (rawOptional.isEmpty() || rawOptional.get().alpha().value() == ubyte.MAX_VALUE.value()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean());
        assert(length < COMMAND_CAPACITY);
        var data = new PxRawColour[length];
        for (int i = 0; i < length; i++) {
            data[i] = PxRawColour.of(artboard.readRaw(startX + i, y).get());
        }
        this.data = data;
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        int x = cursor.x();
        int y = cursor.y();
        int length = this.data.length;
        for (int i = 0; i < length; i++) {
            artboard.writeRaw(x + i, y, this.data[i].colour());
        }
        return new DrawCursor(x + length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        assert(dataType == PxColourValueType.RAW_COLOUR);
        int length = this.data.length;
        MemoryAccess.setByteAtOffset(data, offset, (byte)0x9e);
        MemoryAccess.setByteAtOffset(data, offset + 1, (byte)length);
        offset += COMMAND_LENGTH;
        for (int i = 0; i < length; i++, offset += dataType.nativeByteSize()) {
            var temp = PxRawColour.of(this.data[i].colour().reverseAlpha());
            temp.toNative(data, offset, dataType, palette);
        }
    }

    @Override
    public long commandLength() {
        return COMMAND_LENGTH;
    }

    @Override
    public long dataLength() {
        return this.data.length;
    }
}
