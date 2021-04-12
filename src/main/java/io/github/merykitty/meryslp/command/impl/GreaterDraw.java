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
public class GreaterDraw implements Command {
    private static final long COMMAND_LENGTH = 2;
    private static final int COMMAND_CAPACITY = 1 << 12;

    private PxRawColour[] data;

    public GreaterDraw(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        ubyte nextByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1));
        int dataLength = ((cmdByte.value() & 0xf0) << 4) | nextByte.value();
        this.data = new PxRawColour[dataLength];
        long currentOffset = offset + COMMAND_LENGTH;
        for (int i = 0; i < dataLength; i++) {
            this.data[i] = PxRawColour.ofNativeData(data, currentOffset, dataType, palette);
            currentOffset += dataType.nativeByteSize();
        }
    }

    public GreaterDraw(Artboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length < Commands.LESSER_THRESHOLD) {
                return false;
            }
            for (int x = startX; x < endX; x++) {
                if (artboard.readRaw(x, y).isEmpty()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
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
        int length = this.data.length;
        MemoryAccess.setByteAtOffset(data, offset, (byte)(((length >>> 4) & 0xf0) | 0x02));
        MemoryAccess.setByteAtOffset(data, offset + 1, (byte)length);
        offset += 2;
        for (int i = 0; i < length; i++, offset += dataType.nativeByteSize()) {
            this.data[i].toNative(data, offset, dataType, palette);
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
