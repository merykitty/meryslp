package io.github.merykitty.slpprocessor.command;

import io.github.merykitty.slpprocessor.image.Artboard;
import io.github.merykitty.slpprocessor.image.Palette;
import io.github.merykitty.slpprocessor.image.PxColourValueType;
import jdk.incubator.foreign.MemorySegment;

public interface Command {
     DrawCursor draw(Artboard artboard, DrawCursor cursor);

     void toNative(MemorySegment data, long offset, PxColourValueType dataType, Palette palette);

     long commandLength();

     long dataLength();

     default long nativeByteSize(PxColourValueType dataType) {
          return commandLength() + dataLength() * dataType.nativeByteSize();
     }
}
