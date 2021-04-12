package io.github.merykitty.meryslp.command.impl;

import io.github.merykitty.meryslp.command.Command;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.image.Artboard;
import io.github.merykitty.meryslp.image.Palette;
import io.github.merykitty.meryslp.image.PxColourValueType;
import io.github.merykitty.meryslp.image.PxPlayerColour;
import io.github.merykitty.meryslp.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class PlayerColourDraw implements Command {
    private static final int CMD_BYTE_CAPACITY = 1 << 4;
    private static final int COMMAND_CAPACITY = 1 << 8;

    private int commandLength;
    private PxPlayerColour[] data;

    public PlayerColourDraw(MemorySegment data, long offset, PxColourValueType type) {
        ubyte cmdByte = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
        int dataLength = cmdByte.value() >>> 4;
        int commandLength;
        if (dataLength != 0) {
            commandLength = 1;
        } else {
            commandLength = 2;
            dataLength = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1)).value();
        }
        this.commandLength = commandLength;
        this.data = new PxPlayerColour[dataLength];
        long currentOffset = offset + commandLength;
        for (int i = 0; i < dataLength; i++) {
            this.data[i] = PxPlayerColour.ofNativeData(data, currentOffset, type);
            currentOffset += type.nativeByteSize();
        }
    }

    public PlayerColourDraw(Artboard artboard, int startX, int endX, int y) {
        int length = endX - startX;
        BooleanSupplier verifier = () -> {
            for (int x = startX; x < endX; x++) {
                if (artboard.readPlayer(x, y).isEmpty()) {
                    return false;
                }
            }
            return true;
        };
        assert(verifier.getAsBoolean() && length < COMMAND_CAPACITY);
        var data = new PxPlayerColour[length];
        for (int i = 0; i < length; i++) {
            data[i] = PxPlayerColour.of(artboard.readPlayer(startX + i, y).get());
        }
        this.data = data;
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
        int length = this.data.length;
        for (int i = 0; i < length; i++) {
            artboard.writePlayer(x + i, y, this.data[i].shade());
        }
        return new DrawCursor(x + length, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        int length = this.data.length;
        if (this.commandLength == 1) {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(length << 4 | 0x06));
        } else {
            MemoryAccess.setByteAtOffset(data, offset, (byte)(0x06));
            MemoryAccess.setByteAtOffset(data, offset + 1, (byte)length);
        }
        offset += this.commandLength;
        for (int i = 0; i < length; i++, offset += dataType.nativeByteSize()) {
            this.data[i].toNative(data, offset, dataType);
        }
    }

    @Override
    public long commandLength() {
        return this.commandLength;
    }

    @Override
    public long dataLength() {
        return this.data.length;
    }
}
