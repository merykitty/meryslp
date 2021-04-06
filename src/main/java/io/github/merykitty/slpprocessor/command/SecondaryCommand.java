package io.github.merykitty.slpprocessor.command;

import io.github.merykitty.slpprocessor.image.SecFrameType;
import io.github.merykitty.slpprocessor.image.SecondaryArtboard;
import jdk.incubator.foreign.MemorySegment;

public interface SecondaryCommand {
    DrawCursor draw(SecondaryArtboard artboard, DrawCursor cursor);

    void toNative(MemorySegment data, long offset, SecFrameType type);

    long commandLength();

    long dataLength();

    default long nativeByteSize() {
        return commandLength() + dataLength();
    }
}
