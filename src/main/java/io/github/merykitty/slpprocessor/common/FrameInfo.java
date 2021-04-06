package io.github.merykitty.slpprocessor.common;

import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static jdk.incubator.foreign.MemoryLayouts.*;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;

import io.github.merykitty.slpprocessor.misc.*;

@__primitive__
public class FrameInfo {
    private static final MemoryLayout NATIVE_LAYOUT = MemoryLayout.ofStruct(
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("cmd_table_offset").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("outline_table_offset").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("palette_offset").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("properties").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("width").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("height").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("hotspot_x").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("hotspot_y").withBitAlignment(8L)
    );

    private static final long NATIVE_SIZE = NATIVE_LAYOUT.byteSize();

    private static final VarHandle CMD_TABLE_OFFSET_HANDLE;
    private static final VarHandle OUTLINE_TABLE_OFFSET_HANDLE;
    private static final VarHandle PALETTE_OFFSET_HANDLE;
    private static final VarHandle PROPERTIES_HANDLE;
    private static final VarHandle WIDTH_HANDLE;
    private static final VarHandle HEIGHT_HANDLE;
    private static final VarHandle HOTSPOT_X_HANDLE;
    private static final VarHandle HOTSPOT_Y_HANDLE;

    static {
        CMD_TABLE_OFFSET_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("cmd_table_offset")).withInvokeExactBehavior();
        OUTLINE_TABLE_OFFSET_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("outline_table_offset")).withInvokeExactBehavior();
        PALETTE_OFFSET_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("palette_offset")).withInvokeExactBehavior();
        PROPERTIES_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("properties")).withInvokeExactBehavior();
        WIDTH_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("width")).withInvokeExactBehavior();
        HEIGHT_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("height")).withInvokeExactBehavior();
        HOTSPOT_X_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("hotspot_x")).withInvokeExactBehavior();
        HOTSPOT_Y_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("hotspot_y")).withInvokeExactBehavior();
    }

    private uint cmdTableOffset;
    private uint outlineTableOffset;
    private uint paletteOffset;
    private int properties;
    private int width;
    private int height;
    private int hotspotX;
    private int hotspotY;

    private FrameInfo(uint cmdTableOffset, uint outlineTableOffset, uint paletteOffset, int properties, int width, int height, int hotspotX, int hotspotY) {
        this.cmdTableOffset = cmdTableOffset;
        this.outlineTableOffset = outlineTableOffset;
        this.paletteOffset = paletteOffset;
        this.properties = properties;
        this.width = width;
        this.height = height;
        this.hotspotX = hotspotX;
        this.hotspotY = hotspotY;
    }

    public static FrameInfo ofNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        uint cmdTableOffset = new uint((int)CMD_TABLE_OFFSET_HANDLE.get(data));
        uint outlineTableOffset = new uint((int)OUTLINE_TABLE_OFFSET_HANDLE.get(data));
        uint paletteOffset = new uint((int)PALETTE_OFFSET_HANDLE.get(data));
        int properties = (int)PROPERTIES_HANDLE.get(data);
        int width = (int)WIDTH_HANDLE.get(data);
        int height = (int)HEIGHT_HANDLE.get(data);
        int hotspotX = (int)HOTSPOT_X_HANDLE.get(data);
        int hotspotY = (int)HOTSPOT_Y_HANDLE.get(data);
        return new FrameInfo(cmdTableOffset, outlineTableOffset, paletteOffset, properties, width, height, hotspotX, hotspotY);
    }

    public Builder builder() {
        return new Builder(this);
    }

    public static Builder emptyBuilder() {
        return new Builder();
    }

    public void toNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        CMD_TABLE_OFFSET_HANDLE.set(data, this.cmdTableOffset.signed());
        OUTLINE_TABLE_OFFSET_HANDLE.set(data, this.outlineTableOffset.signed());
        PALETTE_OFFSET_HANDLE.set(data, this.paletteOffset.signed());
        PROPERTIES_HANDLE.set(data, this.properties);
        WIDTH_HANDLE.set(data, this.width);
        HEIGHT_HANDLE.set(data, this.height);
        HOTSPOT_X_HANDLE.set(data, this.hotspotX);
        HOTSPOT_Y_HANDLE.set(data, this.hotspotY);
    }

    public long cmdTableOffset() {
        return this.cmdTableOffset.value();
    }

    public long outlineTableOffset() {
        return this.outlineTableOffset.value();
    }

    public long paletteOffset() {
        return this.paletteOffset.value();
    }

    public int properties() {
        return this.properties;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public int hotspotX() {
        return this.hotspotX;
    }

    public int hotspotY() {
        return this.hotspotY;
    }

    public static long nativeByteSize() {
        return NATIVE_SIZE;
    }

    public static class Builder {
        private uint cmdTableOffset;
        private uint outlineTableOffset;
        private uint paletteOffset;
        private int properties;
        private int width;
        private int height;
        private int hotspotX;
        private int hotspotY;

        private Builder() {}
        private Builder(FrameInfo frameInfo) {
            this.cmdTableOffset = frameInfo.cmdTableOffset;
            this.outlineTableOffset = frameInfo.outlineTableOffset;
            this.paletteOffset = frameInfo.paletteOffset;
            this.properties = frameInfo.properties;
            this.width = frameInfo.width;
            this.height = frameInfo.height;
            this.hotspotX = frameInfo.hotspotX;
            this.hotspotY = frameInfo.hotspotY;
        }

        public Builder cmdTableOffset(uint cmdTableOffset) {
            this.cmdTableOffset = cmdTableOffset;
            return this;
        }

        public Builder outlineTableOffset(uint outlineTableOffset) {
            this.outlineTableOffset = outlineTableOffset;
            return this;
        }

        public Builder paletteOffset(uint paletteOffset) {
            this.paletteOffset = paletteOffset;
            return this;
        }

        public Builder properties(int properties) {
            this.properties = properties;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder hotspotX(int hotspotX) {
            this.hotspotX = hotspotX;
            return this;
        }

        public Builder hotspotY(int hotspotY) {
            this.hotspotY = hotspotY;
            return this;
        }

        public FrameInfo build() {
            return new FrameInfo(cmdTableOffset, outlineTableOffset, paletteOffset, properties, width, height, hotspotX, hotspotY);
        }
    }
}