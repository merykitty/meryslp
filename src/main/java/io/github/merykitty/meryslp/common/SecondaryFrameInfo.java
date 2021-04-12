package io.github.merykitty.meryslp.common;

import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;
import static jdk.incubator.foreign.MemoryLayouts.*;

import io.github.merykitty.meryslp.misc.uint;
import io.github.merykitty.meryslp.misc.ubyte;

@__primitive__
public class SecondaryFrameInfo {
    private static final MemoryLayout NATIVE_LAYOUT = MemoryLayout.ofStruct(
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_cmd_table_offset").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_outline_table_offset").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_null").withBitAlignment(8L),
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("sec_properties_lo").withBitAlignment(8L),
            JAVA_BYTE.withOrder(LITTLE_ENDIAN).withName("sec_properties_hi").withBitAlignment(8L),
            JAVA_BYTE.withOrder(LITTLE_ENDIAN).withName("sec_frame_type").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_width").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_height").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_hotspot_x").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("sec_hotspot_y").withBitAlignment(8L)
    );

    private static final long NATIVE_SIZE = NATIVE_LAYOUT.byteSize();

    private static final VarHandle CMD_TABLE_OFFSET_HANDLE;
    private static final VarHandle OUTLINE_TABLE_OFFSET_HANDLE;
    private static final VarHandle NUL_HANDLE;
    private static final VarHandle PROPERTIES_LO_HANDLE;
    private static final VarHandle PROPERTIES_HI_HANDLE;
    private static final VarHandle FRAME_TYPE_HANDLE;
    private static final VarHandle WIDTH_HANDLE;
    private static final VarHandle HEIGHT_HANDLE;
    private static final VarHandle HOTSPOT_X_HANDLE;
    private static final VarHandle HOTSPOT_Y_HANDLE;

    static {
        CMD_TABLE_OFFSET_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("sec_cmd_table_offset")).withInvokeExactBehavior();
        OUTLINE_TABLE_OFFSET_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("sec_outline_table_offset")).withInvokeExactBehavior();
        NUL_HANDLE = NATIVE_LAYOUT.varHandle(int.class, groupElement("sec_null")).withInvokeExactBehavior();
        PROPERTIES_LO_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("sec_properties_lo")).withInvokeExactBehavior();
        PROPERTIES_HI_HANDLE = NATIVE_LAYOUT.varHandle(byte.class, groupElement("sec_properties_hi")).withInvokeExactBehavior();
        FRAME_TYPE_HANDLE = NATIVE_LAYOUT.varHandle(byte.class,groupElement("sec_frame_type")).withInvokeExactBehavior();
        WIDTH_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("sec_width")).withInvokeExactBehavior();
        HEIGHT_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("sec_height")).withInvokeExactBehavior();
        HOTSPOT_X_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("sec_hotspot_x")).withInvokeExactBehavior();
        HOTSPOT_Y_HANDLE = NATIVE_LAYOUT.varHandle(int.class,groupElement("sec_hotspot_y")).withInvokeExactBehavior();
    }

    private uint cmdTableOffset;
    private uint outlineTableOffset;
    private int nul;
    private short propertiesLo;
    private byte propertiesHi;
    private ubyte frameType;
    private int width;
    private int height;
    private int hotspotX;
    private int hotspotY;

    private SecondaryFrameInfo(uint cmdTableOffset, uint outlineTableOffset, int nul, short propertiesLo, byte propertiesHi, ubyte frameType, int width, int height, int hotspotX, int hotspotY) {
        this.cmdTableOffset = cmdTableOffset;
        this.outlineTableOffset = outlineTableOffset;
        this.nul = nul;
        this.propertiesLo = propertiesLo;
        this.propertiesHi = propertiesHi;
        this.frameType = frameType;
        this.width = width;
        this.height = height;
        this.hotspotX = hotspotX;
        this.hotspotY = hotspotY;
    }

    public static SecondaryFrameInfo ofNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        uint cmdTableOffset = new uint((int)CMD_TABLE_OFFSET_HANDLE.get(data));
        uint outlineTableOffset = new uint((int)OUTLINE_TABLE_OFFSET_HANDLE.get(data));
        int nul = (int)NUL_HANDLE.get(data);
        short propertiesLo = (short)PROPERTIES_LO_HANDLE.get(data);
        byte propertiesHi = (byte)PROPERTIES_HI_HANDLE.get(data);
        ubyte frameType = new ubyte((byte)FRAME_TYPE_HANDLE.get(data));
        int width = (int)WIDTH_HANDLE.get(data);
        int height = (int)HEIGHT_HANDLE.get(data);
        int hotspotX = (int)HOTSPOT_X_HANDLE.get(data);
        int hotspotY = (int)HOTSPOT_Y_HANDLE.get(data);
        return new SecondaryFrameInfo(cmdTableOffset, outlineTableOffset, nul, propertiesLo, propertiesHi, frameType, width, height, hotspotX, hotspotY);
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
        NUL_HANDLE.set(data, this.nul);
        PROPERTIES_LO_HANDLE.set(data, this.propertiesLo);
        PROPERTIES_HI_HANDLE.set(data, this.propertiesHi);
        FRAME_TYPE_HANDLE.set(data, this.frameType.signed());
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

    public int nul() {
        return this.nul;
    }

    public int properties() {
        return this.propertiesLo | (this.propertiesHi << 16);
    }

    public int frameType() {
        return this.frameType.value();
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
        private int nul;
        private short propertiesLo;
        private byte propertiesHi;
        private ubyte frameType;
        private int width;
        private int height;
        private int hotspotX;
        private int hotspotY;

        private Builder() {}
        private Builder(SecondaryFrameInfo frameInfo) {
            this.cmdTableOffset = frameInfo.cmdTableOffset;
            this.outlineTableOffset = frameInfo.outlineTableOffset;
            this.nul = frameInfo.nul;
            this.propertiesLo = frameInfo.propertiesLo;
            this.propertiesHi = frameInfo.propertiesHi;
            this.frameType = frameInfo.frameType;
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

        public Builder nul(int nul) {
            this.nul = nul;
            return this;
        }

        public Builder properties(int properties) {
            this.propertiesLo = (short)(properties & 0xffff);
            this.propertiesHi = (byte)(properties >>> 16);
            return this;
        }

        public Builder frameType(ubyte frameType) {
            this.frameType = frameType;
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

        public SecondaryFrameInfo build() {
            return new SecondaryFrameInfo(cmdTableOffset, outlineTableOffset, nul, propertiesLo, propertiesHi, frameType, width, height, hotspotX, hotspotY);
        }
    }
}
