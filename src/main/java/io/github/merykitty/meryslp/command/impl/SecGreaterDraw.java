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
public class SecGreaterDraw implements SecondaryCommand {
    private static final long COMMAND_LENGTH = 2;
    private static final int COMMAND_CAPACITY = 1 << 12;

    private ubyte[] data;

    public SecGreaterDraw(MemorySegment data, long offset, SecFrameType frameType) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        ubyte nextByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1));
        int dataLength = ((cmdByte.value() & 0xf0) << 4) | nextByte.value();
        var shadeData = new ubyte[dataLength];
        long currentOffset = offset + COMMAND_LENGTH;
        for (int i = 0; i < dataLength; i++) {
            shadeData[i] = new ubyte(MemoryAccess.getByteAtOffset(data, currentOffset));
            currentOffset++;
        }
        this.data = shadeData;
    }

    public SecGreaterDraw(SecondaryArtboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            if (length < Commands.LESSER_THRESHOLD) {
                return false;
            }
            for (int x = startX; x < endX; x++) {
                if (artboard.readMain(x, y).isEmpty()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        var data = new ubyte[length];
        for (int i = 0; i < length; i++) {
            data[i] = artboard.readMain(startX + i, y).get();
        }
        this.data = data;
    }

    @Override
    public DrawCursor draw(SecondaryArtboard artboard, DrawCursor cursor) {
        int x = cursor.x();
        int y = cursor.y();
        int length = this.data.length;
        for (int i = 0; i < length; i++) {
            artboard.writeMain(x + i, y, this.data[i]);
        }
        return new DrawCursor(x + length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, SecFrameType type) {
        int length = this.data.length;
        MemoryAccess.setByteAtOffset(data, offset, (byte)(((length >>> 4) & 0xf0) | 0x02));
        MemoryAccess.setByteAtOffset(data, offset + 1, (byte)length);
        offset += 2;
        for (int i = 0; i < length; i++, offset++) {
            MemoryAccess.setByteAtOffset(data, offset, type.reverse(this.data[i]).signed());
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
