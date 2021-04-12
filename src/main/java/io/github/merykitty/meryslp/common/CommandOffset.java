package io.github.merykitty.meryslp.common;

import java.lang.invoke.VarHandle;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static jdk.incubator.foreign.MemoryLayouts.*;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;

import io.github.merykitty.meryslp.misc.*;

@__primitive__
public class CommandOffset {
    private static final MemoryLayout NATIVE_LAYOUT = MemoryLayout.ofStruct(
        JAVA_INT.withOrder(LITTLE_ENDIAN).withName("offset").withBitAlignment(8L)
    );

    private static final long NATIVE_SIZE = NATIVE_LAYOUT.byteSize();

    private static final VarHandle OFFSET_HANDLE = NATIVE_LAYOUT.varHandle(int.class, groupElement("offset")).withInvokeExactBehavior();

    private uint offset;

    public CommandOffset(uint offset) {
        this.offset = offset;
    }

    public static CommandOffset ofNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        uint cmdOffset = new uint((int)OFFSET_HANDLE.get(data));
        return new CommandOffset(cmdOffset);
    }

    public void toNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        OFFSET_HANDLE.set(data, this.offset.signed());
    }

    public long offset() {
        return this.offset.value();
    }

    public static long nativeByteSize() {
        return NATIVE_SIZE;
    }
}
