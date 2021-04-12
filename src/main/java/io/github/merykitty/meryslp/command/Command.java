package io.github.merykitty.meryslp.command;

import io.github.merykitty.meryslp.image.Artboard;
import io.github.merykitty.meryslp.image.Palette;
import io.github.merykitty.meryslp.image.PxColourValueType;
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
