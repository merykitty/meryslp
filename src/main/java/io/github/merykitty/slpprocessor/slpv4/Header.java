package io.github.merykitty.slpprocessor.slpv4;

import java.lang.invoke.VarHandle;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;
import static jdk.incubator.foreign.MemoryLayout.PathElement.sequenceElement;
import static jdk.incubator.foreign.MemoryLayouts.*;

import org.json.JSONObject;

import io.github.merykitty.slpprocessor.common.Version;
import static io.github.merykitty.slpprocessor.common.SLPFile.*;

@__primitive__
public class Header {
    private static final MemoryLayout NATIVE_LAYOUT = MemoryLayout.ofStruct(
            MemoryLayout.ofSequence(4, JAVA_BYTE).withName("version").withBitAlignment(8L),
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("num_frames").withBitAlignment(8L),
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("type").withBitAlignment(8L),
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("num_directions").withBitAlignment(8L),
            JAVA_SHORT.withOrder(LITTLE_ENDIAN).withName("frames_per_direction").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("palette_id").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("offset_main").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("offset_secondary").withBitAlignment(8L),
            PAD_64
    );

    private static final long NATIVE_SIZE = NATIVE_LAYOUT.byteSize();

    private static final VarHandle VERSION_HANDLE;
    private static final VarHandle NUM_FRAMES_HANDLE;
    private static final VarHandle TYPE_HANDLE;
    private static final VarHandle NUM_DIRECTIONS_HANDLE;
    private static final VarHandle FRAMES_PER_DIRECTION_HANDLE;
    private static final VarHandle PALETTE_ID_HANDLE;
    private static final VarHandle OFFSET_MAIN_HANDLE;
    private static final VarHandle OFFSET_SECONDARY_HANDLE;

    static {
        VERSION_HANDLE = NATIVE_LAYOUT.varHandle(byte.class, groupElement("version"), sequenceElement(0, 1)).withInvokeExactBehavior();
        NUM_FRAMES_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("num_frames")).withInvokeExactBehavior();
        TYPE_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("type")).withInvokeExactBehavior();
        NUM_DIRECTIONS_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("num_directions")).withInvokeExactBehavior();
        FRAMES_PER_DIRECTION_HANDLE = NATIVE_LAYOUT.varHandle(short.class, groupElement("frames_per_direction")).withInvokeExactBehavior();
        PALETTE_ID_HANDLE = NATIVE_LAYOUT.varHandle(int.class, groupElement("palette_id")).withInvokeExactBehavior();
        OFFSET_MAIN_HANDLE = NATIVE_LAYOUT.varHandle(int.class, groupElement("offset_main")).withInvokeExactBehavior();
        OFFSET_SECONDARY_HANDLE = NATIVE_LAYOUT.varHandle(int.class, groupElement("offset_secondary")).withInvokeExactBehavior();
    }

    private Version version;
    private short numFrames;
    private short type;
    private short numDirections;
    private short framesPerDirection;
    private int paletteId;
    private int offsetMain;
    private int offsetSecondary;

    private Header(Version version, short numFrames, short type, short numDirections, short framesPerDirection, int paletteId, int offsetMain, int offsetSecondary) {
        this.version = version;
        this.numFrames = numFrames;
        this.type = type;
        this.numDirections = numDirections;
        this.framesPerDirection = framesPerDirection;
        this.paletteId = paletteId;
        this.offsetMain = offsetMain;
        this.offsetSecondary = offsetSecondary;
    }

    public static Header ofNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        var version = Version.ofNativeData(data.asSlice(VERSION_OFFSET, VERSION_SIZE));
        short numFrames = (short)NUM_FRAMES_HANDLE.get(data);
        short type = (short)TYPE_HANDLE.get(data);
        short numDirections = (short)NUM_DIRECTIONS_HANDLE.get(data);
        short framesPerDirection = (short)FRAMES_PER_DIRECTION_HANDLE.get(data);
        int paletteId = (int)PALETTE_ID_HANDLE.get(data);
        int offsetMain = (int)OFFSET_MAIN_HANDLE.get(data);
        int offsetSecondary = (int)OFFSET_SECONDARY_HANDLE.get(data);
        return new Header(version, numFrames, type, numDirections, framesPerDirection, paletteId, offsetMain, offsetSecondary);
    }

    public static Header importJson(JSONObject json) {
        var version = Version.of(json.getString("version"));
        short numFrames = (short)json.getInt("num_frames");
        short type = (short)json.getInt("type");
        short numDirections = (short)json.getInt("num_directions");
        short framesPerDirection = (short)json.getInt("frames_per_direction");
        int paletteId = json.getInt("palette_id");
        assert(version.major() == 4);
        assert(numDirections == 1 && framesPerDirection == numFrames);
        return new Header(version, numFrames, type, numDirections, framesPerDirection, paletteId, 0, 0);
    }

    public void toNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        VERSION_HANDLE.set(data, 0L, (byte)(this.version.major() + '0'));
        VERSION_HANDLE.set(data, 1L, (byte)('.'));
        VERSION_HANDLE.set(data, 2L, (byte)(this.version.minor() + '0'));
        VERSION_HANDLE.set(data, 3L, (byte)(this.version.release() - 'a' + 'A'));
        NUM_FRAMES_HANDLE.set(data, this.numFrames);
        TYPE_HANDLE.set(data, this.type);
        NUM_DIRECTIONS_HANDLE.set(data, this.numDirections);
        FRAMES_PER_DIRECTION_HANDLE.set(data, this.framesPerDirection);
        PALETTE_ID_HANDLE.set(data, this.paletteId);
        OFFSET_MAIN_HANDLE.set(data, this.offsetMain);
        OFFSET_SECONDARY_HANDLE.set(data, this.offsetSecondary);
    }

    public void exportJson(JSONObject json) {
        json.put("version", this.version().toString());
        json.put("num_frames", this.numFrames());
        json.put("type", this.type());
        json.put("num_directions", this.numDirections());
        json.put("frames_per_direction", this.framesPerDirection());
        json.put("palette_id", this.paletteId());
    }

    public Builder builder() {
        return new Builder(this);
    }

    public static Builder emptyBuilder() {
        return new Builder();
    }

    public static long nativeByteSize() {
        return NATIVE_SIZE;
    }

    public Version version() {
        return this.version;
    }

    public int numFrames() {
        return this.numFrames;
    }

    public int type() {
        return this.type;
    }

    public int numDirections() {
        return this.numDirections;
    }

    public int framesPerDirection() {
        return this.framesPerDirection;
    }

    public int paletteId() {
        return this.paletteId;
    }

    public long offsetMain() {
        return this.offsetMain;
    }

    public long offsetSecondary() {
        return this.offsetSecondary;
    }

    public static class Builder {
        private Version version;
        private short numFrames;
        private short type;
        private short numDirections;
        private short framesPerDirection;
        private int paletteId;
        private int offsetMain;
        private int offsetSecondary;

        private Builder() {}
        private Builder(Header header) {
            this.version = header.version;
            this.numFrames = header.numFrames;
            this.type = header.type;
            this.numDirections = header.numDirections;
            this.framesPerDirection = header.framesPerDirection;
            this.paletteId = header.paletteId;
            this.offsetMain = header.offsetMain;
            this.offsetSecondary = header.offsetSecondary;
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Builder numFrames(short numFrames) {
            this.numFrames = numFrames;
            return this;
        }

        public Builder type(short type) {
            this.type = type;
            return this;
        }

        public Builder numDirections(short numDirections) {
            this.numDirections = numDirections;
            return this;
        }

        public Builder framesPerDirection(short framesPerDirection) {
            this.framesPerDirection = framesPerDirection;
            return this;
        }

        public Builder paletteId(int paletteId) {
            this.paletteId = paletteId;
            return this;
        }

        public Builder offsetMain(int offsetMain) {
            this.offsetMain = offsetMain;
            return this;
        }

        public Builder offsetSecondary(int offsetSecondary) {
            this.offsetSecondary = offsetSecondary;
            return this;
        }

        public Header build() {
            return new Header(version, numFrames, type, numDirections, framesPerDirection, paletteId, offsetMain, offsetSecondary);
        }
    }
}
