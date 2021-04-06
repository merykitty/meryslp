package io.github.merykitty.slpprocessor.command.impl;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.image.Artboard;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PxColourValueType;
import io.github.merykitty.slpprocessor.image.PxPlayerColour;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import java.util.function.BooleanSupplier;

@__primitive__
public class Outline implements Command {
    private PxPlayerColour colour;

    public Outline(MemorySegment data, long offset, PxColourValueType type) {
        this.colour = PxPlayerColour.ofNativeData(data, offset + 1, type);
    }

    public Outline(Artboard artboard, int startX, int endX, int y) {
        var outlineOptional = artboard.readOutline(startX, y);
        BooleanSupplier verifier = () -> {
            return outlineOptional.isPresent() && endX - startX == 1;
        };
        assert(verifier.getAsBoolean());
        this.colour = PxPlayerColour.of(outlineOptional.get());
    }

    @Override
    public DrawCursor draw(Artboard artboard, DrawCursor cursor) {
        int x = cursor.x();
        int y = cursor.y();
        artboard.writeOutline(x, y, this.colour.shade());
        return new DrawCursor(x + 1, y);
    }

    @Override
    public void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette) {
        MemoryAccess.setByteAtOffset(data, offset, (byte)0x4e);
        this.colour.toNative(data, offset + 1, dataType);
    }

    @Override
    public long commandLength() {
        return 1;
    }

    @Override
    public long dataLength() {
        return 1;
    }
}
