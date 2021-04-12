package io.github.merykitty.meryslp.slpv3;

import io.github.merykitty.meryslp.common.Frame;
import io.github.merykitty.meryslp.common.FrameInfo;
import io.github.merykitty.meryslp.image.PaletteContainer;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import io.github.merykitty.meryslp.common.SLPFile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.github.merykitty.meryslp.common.SLPFiles.roundUpMod16;

@__primitive__
public class SLPFileVer3 implements SLPFile {
    private Header header;
    private Frame[] frameList;

    private SLPFileVer3(Header header, Frame[] frameList) {
        this.header = header;
        this.frameList = frameList;
    }

    public static SLPFileVer3 ofNativeData(MemorySegment file, PaletteContainer palettes) {
        long currentOffset = 0;
        var header = Header.ofNativeData(file, currentOffset);
        var version = header.version();
        int numFrames = header.numFrames();
        currentOffset += Header.nativeByteSize();
        var frameInfoList = new FrameInfo[numFrames];
        for (int i = 0; i < numFrames; i++) {
            var tempFrameInfo = FrameInfo.ofNativeData(file, currentOffset);
            frameInfoList[i] = tempFrameInfo;
            currentOffset += FrameInfo.nativeByteSize();
        }
        for (long i = currentOffset; i < frameInfoList[0].outlineTableOffset(); i++) {
            assert(MemoryAccess.getByteAtOffset(file, i) == 0);
        }
        var frameList = new Frame[numFrames];
        for (int i = 0; i < numFrames; i++) {
            var frameInfo = frameInfoList[i];
            long nextFrameOffset;
            if (i < numFrames - 1) {
                nextFrameOffset = frameInfoList[i + 1].outlineTableOffset();
            } else {
                nextFrameOffset = file.byteSize();
            }
            var frame = Frame.ofNativeData(file, frameInfo, nextFrameOffset, version, palettes);
            frameList[i] = frame;
        }
        return new SLPFileVer3(header, frameList);
    }

    public static SLPFileVer3 importGraphics(JSONObject meta, Path importFolder, PaletteContainer palettes) throws IOException {
        var header = Header.importJson(meta);
        assert(header.version().major() < 4);
        var frameDataList = meta.getJSONArray("frames");
        int numFrames = header.numFrames();
        assert(frameDataList.length() == numFrames);
        var frameList = new Frame[numFrames];
        var frameFolder = importFolder.resolve("graphics");
        for (int i = 0; i < numFrames; i++) {
            var frame = Frame.importFrame(frameDataList.getJSONObject(i), frameFolder, i);
            frameList[i] = frame;
        }
        return new SLPFileVer3(header, frameList);
    }

    @Override
    public void exportGraphics(Path exportFolder) throws IOException {
        var meta = new JSONObject();
        this.header.exportJson(meta);
        var frameDataList = new JSONArray();
        meta.put("frames", frameDataList);
        int numFrames = this.frameList.length;
        var frameFolder = exportFolder.resolve("graphics");
        Files.createDirectories(frameFolder);
        for (int i = 0; i < numFrames; i++) {
            var frame = this.frameList[i];
            var frameData = frame.exportFrame(frameFolder, i);
            frameDataList.put(frameData);
        }
        Files.writeString(exportFolder.resolve("meta.json"), meta.toString(4), StandardCharsets.UTF_8);
    }

    @Override
    public MemorySegment toNativeData(PaletteContainer palettes) {
        long requireSize = nativeByteSize();
        var data = MemorySegment.allocateNative(requireSize);
        long currentOffset = 0;
        this.header.toNativeData(data, currentOffset);
        var version = this.header.version();
        int numFrames = this.header.numFrames();
        long frameInfoOffset = roundUpMod16(currentOffset + Header.nativeByteSize());
        currentOffset += frameInfoOffset;
        var frameInfoList = new FrameInfo[numFrames];
        currentOffset += numFrames * FrameInfo.nativeByteSize();
        for (int i = 0; i < numFrames; i++) {
            currentOffset = roundUpMod16(currentOffset);
            var frame = this.frameList[i];
            var info = frame.toNativeData(data, currentOffset, version, palettes);
            frameInfoList[i] = info.frameInfo();
            currentOffset = info.currentOffset();
        }
        currentOffset = frameInfoOffset;
        for (int i = 0; i < numFrames; i++) {
            frameInfoList[i].toNativeData(data, currentOffset);
            currentOffset += FrameInfo.nativeByteSize();
        }
        return data;
    }

    private long nativeByteSize() {
        var version = this.header.version();
        long result = 0;
        result += Header.nativeByteSize();
        int numFrames = this.header.numFrames();
        result += numFrames * FrameInfo.nativeByteSize();
        for (int i = 0; i < numFrames; i++) {
            var frame = this.frameList[i];
            result = roundUpMod16(result);
            result += frame.nativeByteSize(version);
        }
        return result;
    }
}
