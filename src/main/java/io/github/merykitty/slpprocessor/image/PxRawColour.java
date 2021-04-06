package io.github.merykitty.slpprocessor.image;

import io.github.merykitty.slpprocessor.misc.*;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

@__primitive__
public class PxRawColour {
    ubyte displayModifier;
    RawColour colour;

    private PxRawColour(ubyte displayModifier, RawColour colour) {
        this.displayModifier = displayModifier;
        this.colour = colour;
    }

    private PxRawColour(RawColour colour) {
        this(ubyte.MAX_VALUE, colour);
    }

    public static PxRawColour ofNativeData(MemorySegment data, long offset, PxColourValueType type, Palette palette) {
        if (type == PxColourValueType.WITHOUT_MODIFIER) {
            ubyte colourId = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
            return new PxRawColour(palette.colour(colourId));
        } else if (type == PxColourValueType.WITH_MODIFIER) {
            ubyte displayModifier = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
            ubyte colourId = new ubyte(MemoryAccess.getByteAtOffset(data, offset + 1));
            assert(displayModifier == ubyte.MAX_VALUE) : ("Received displayer modifier: " + displayModifier.value());
            return new PxRawColour(displayModifier, palette.colour(colourId));
        } else {
            assert(palette.isEmpty());
            int bgraValue = MemoryAccess.getIntAtOffset(data, offset);
            return new PxRawColour(RawColour.fromBGRA(bgraValue));
        }
    }

    public static PxRawColour of(RawColour colour) {
        return new PxRawColour(ubyte.MAX_VALUE, colour);
    }

    public void toNative(MemorySegment data, long offset, PxColourValueType type, Palette palette) {
        if (type == PxColourValueType.WITHOUT_MODIFIER) {
            MemoryAccess.setByteAtOffset(data, offset, palette.index(this.colour).signed());
        } else if (type == PxColourValueType.WITH_MODIFIER) {
            MemoryAccess.setByteAtOffset(data, offset, this.displayModifier.signed());
            MemoryAccess.setByteAtOffset(data, offset + 1, palette.index(this.colour).signed());
        } else {
            MemoryAccess.setIntAtOffset(data, offset, this.colour.toBGRA());
        }
    }

    public RawColour colour() {
        return this.colour;
    }
}
