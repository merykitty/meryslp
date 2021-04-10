package io.github.merykitty.slpprocessor.slpv3;

import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static jdk.incubator.foreign.MemoryLayout.PathElement.groupElement;
import static jdk.incubator.foreign.MemoryLayout.PathElement.sequenceElement;
import static jdk.incubator.foreign.MemoryLayouts.*;

import org.json.JSONObject;

import io.github.merykitty.slpprocessor.common.Version;
import static io.github.merykitty.slpprocessor.common.SLPFile.*;

public class Header {
    private static final MemoryLayout NATIVE_LAYOUT = MemoryLayout.ofStruct(
            MemoryLayout.ofSequence(4, JAVA_BYTE).withName("version").withBitAlignment(8L),
            JAVA_INT.withOrder(LITTLE_ENDIAN).withName("num_frames").withBitAlignment(8L),
            MemoryLayout.ofSequence(24, JAVA_BYTE).withName("comment").withBitAlignment(8L)
    );

    private static final long NATIVE_SIZE = NATIVE_LAYOUT.byteSize();
    private static final long COMMENT_OFFSET = 8;

    private static final VarHandle NUM_FRAMES_HANDLE;

    static {
        NUM_FRAMES_HANDLE = NATIVE_LAYOUT.varHandle(int.class, groupElement("num_frames")).withInvokeExactBehavior();
    }

    private Version version;
    private int numFrames;
    private String comment;

    private Header(Version version, int numFrames, String comment) {
        this.version = version;
        this.numFrames = numFrames;
        this.comment = comment;
    }

    public static Header ofNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        var version = Version.ofNativeData(data.asSlice(VERSION_OFFSET, VERSION_SIZE));
        int numFrames = (int)NUM_FRAMES_HANDLE.get(data);
        String comment = new String(data.asSlice(COMMENT_OFFSET).toByteArray());
        return new Header(version, numFrames, comment);
    }

    public static Header importJson(JSONObject json) {
        var version = Version.of(json.getString("version"));
        int numFrames = json.getInt("num_frames");
        String comment = json.getString("comment");
        return new Header(version, numFrames, comment);
    }

    public void toNativeData(MemorySegment data, long offset) {
        data = data.asSlice(offset, NATIVE_SIZE);
        data.asSlice(VERSION_OFFSET, VERSION_SIZE).copyFrom(MemorySegment.ofArray(this.version.toString().toUpperCase().getBytes(StandardCharsets.US_ASCII)));
        NUM_FRAMES_HANDLE.set(data, this.numFrames);
        data.asSlice(COMMENT_OFFSET).copyFrom(MemorySegment.ofArray(this.comment.getBytes(StandardCharsets.US_ASCII)));
    }

    public void exportJson(JSONObject json) {
        json.put("version", this.version.toString());
        json.put("num_frames", this.numFrames);
        json.put("comment", this.comment);
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
}
