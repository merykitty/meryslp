package io.github.merykitty.slpprocessor.common;

import java.lang.invoke.VarHandle;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static jdk.incubator.foreign.MemoryLayouts.*;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;

import io.github.merykitty.slpprocessor.misc.*;

@__primitive__
public class FrameRowEdge {
    private static final MemoryLayout NATIVE_LAYOUT = MemoryLayout.ofStruct(
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("left_space").withBitAlignment(8L),
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("right_space").withBitAlignment(8L)
    );

    private static final long NATIVE_SIZE = NATIVE_LAYOUT.byteSize();

    private static final VarHandle LEFT_SPACE_HANDLE;
    private static final VarHandle RIGHT_SPACE_HANDLE;

    static {
        LEFT_SPACE_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("left_space")).withInvokeExactBehavior();
        RIGHT_SPACE_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("right_space")).withInvokeExactBehavior();
    }

    private ushort leftSpace;
    private ushort rightSpace;

    public FrameRowEdge(ushort leftSpace, ushort rightSpace) {
        this.leftSpace = leftSpace;
        this.rightSpace = rightSpace;
    }

    public static FrameRowEdge ofNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        ushort leftSpace = new ushort((short)LEFT_SPACE_HANDLE.get(data));
        ushort rightSpace = new ushort((short)RIGHT_SPACE_HANDLE.get(data));
        return new FrameRowEdge(leftSpace, rightSpace);
    }

    public void toNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        LEFT_SPACE_HANDLE.set(data, this.leftSpace.signed());
        RIGHT_SPACE_HANDLE.set(data, this.rightSpace.signed());
    }

    public int leftSpace() {
        return this.leftSpace.value();
    }

    public int rightSpace() {
        return this.rightSpace.value();
    }

    public static long nativeByteSize() {
        return NATIVE_SIZE;
    }
}
