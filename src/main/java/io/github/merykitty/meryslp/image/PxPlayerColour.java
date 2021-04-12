package io.github.merykitty.meryslp.image;

import io.github.merykitty.meryslp.misc.*;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

@__primitive__
public class PxPlayerColour {
    ubyte shade;
    ubyte displayModifier;

    private PxPlayerColour(ubyte displayModifier, ubyte shade) {
        this.displayModifier = displayModifier;
        this.shade = shade;
    }

    private PxPlayerColour(ubyte shade) {
        this(ubyte.MAX_VALUE, shade);
    }

    public static PxPlayerColour ofNativeData(MemorySegment data, long offset, PxColourValueType type) {
        assert(type != PxColourValueType.RAW_COLOUR);
        if (type == PxColourValueType.WITHOUT_MODIFIER) {
            ubyte shade = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
            return new PxPlayerColour(shade);
        } else {
            ubyte shade = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
            ubyte displayModifier = new ubyte(MemoryAccess.getByteAtOffset(data, offset));
            assert(displayModifier == ubyte.MAX_VALUE) : ("Received display modifier: " + displayModifier.value());
            return new PxPlayerColour(displayModifier, shade);
        }
    }

    public static PxPlayerColour of(ubyte shade) {
        return new PxPlayerColour(ubyte.MAX_VALUE, shade);
    }

    public void toNative(MemorySegment data, long offset, PxColourValueType type) {
        assert(type != PxColourValueType.RAW_COLOUR);
        if (type == PxColourValueType.WITHOUT_MODIFIER) {
            MemoryAccess.setByteAtOffset(data, offset, this.shade.signed());
        } else {
            MemoryAccess.setByteAtOffset(data, offset, this.displayModifier.signed());
            MemoryAccess.setByteAtOffset(data, offset + 1, this.shade.signed());
        }
    }

    public ubyte shade() {
        return this.shade;
    }
}
