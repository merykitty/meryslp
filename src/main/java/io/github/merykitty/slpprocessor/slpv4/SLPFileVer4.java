package io.github.merykitty.slpprocessor.slpv4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.github.merykitty.slpprocessor.common.*;
import io.github.merykitty.slpprocessor.misc.PrimitiveOptional;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import org.json.JSONObject;
import org.json.JSONArray;

import io.github.merykitty.slpprocessor.image.PaletteContainer;
import org.json.JSONTokener;

import static io.github.merykitty.slpprocessor.common.SLPFiles.roundUpMod16;
import static io.github.merykitty.slpprocessor.common.SLPFiles.roundUpMod32;

@__primitive__
public class SLPFileVer4 implements SLPFile {
    private Header header;
    private Frame[] frameList;
    private PrimitiveOptional<SecondaryFrame[]> secFrameList;

    private SLPFileVer4(Header header, Frame[] frameList, SecondaryFrame[] secFrameList) {
        this.header = header;
        this.frameList = frameList;
        this.secFrameList = PrimitiveOptional.ofNullable(secFrameList);
    }

    public static SLPFileVer4 ofNativeData(MemorySegment file, PaletteContainer palettes) {
        long currentOffset = 0;
        var header = Header.ofNativeData(file, currentOffset);
        var version = header.version();
        int numFrames = header.numFrames();
        currentOffset = header.offsetMain();
        assert(currentOffset == 0x20);
        assert((currentOffset & 0x0f) == 0);
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
                if (header.offsetSecondary() != 0) {
                    nextFrameOffset = header.offsetSecondary();
                } else {
                    nextFrameOffset = file.byteSize();
                }
            }
            var frame = Frame.ofNativeData(file, frameInfo, nextFrameOffset, version, palettes);
            frameList[i] = frame;
        }

        currentOffset = header.offsetSecondary();
        SecondaryFrame[] secFrameList;
        if (currentOffset == 0) {
            secFrameList = null;
        } else {
            assert((currentOffset & 0x1f) == 0);
            var secFrameInfoList = new SecondaryFrameInfo[numFrames];
            for (int i = 0; i < numFrames; i++) {
                var tempSecFrameInfo = SecondaryFrameInfo.ofNativeData(file, currentOffset);
                secFrameInfoList[i] = tempSecFrameInfo;
                currentOffset += SecondaryFrameInfo.nativeByteSize();
            }
            for (long i = currentOffset; i < secFrameInfoList[0].outlineTableOffset(); i++) {
                assert(MemoryAccess.getByteAtOffset(file, i) == 0);
            }
            secFrameList = new SecondaryFrame[numFrames];
            for (int i = 0; i < numFrames; i++) {
                var frameInfo = secFrameInfoList[i];
                long nextFrameOffset;
                if (i < numFrames - 1) {
                    nextFrameOffset = secFrameInfoList[i + 1].outlineTableOffset();
                } else {
                    nextFrameOffset = file.byteSize();
                }
                var frame = SecondaryFrame.ofNativeData(file, frameInfo, nextFrameOffset);
                secFrameList[i] = frame;
            }
        }
        return new SLPFileVer4(header, frameList, secFrameList);
    }

    public static SLPFileVer4 importGraphics(JSONObject meta, Path importFolder, PaletteContainer palettes) throws IOException {
        var header = Header.importJson(meta);
        assert(header.version().major() == 4);
        var frameDataList = meta.getJSONArray("frames");
        int numFrames = header.numFrames();
        assert(frameDataList.length() == numFrames);
        var frameList = new Frame[numFrames];
        var frameFolder = importFolder.resolve("graphics");
        for (int i = 0; i < numFrames; i++) {
            var frame = Frame.importFrame(frameDataList.getJSONObject(i), frameFolder, i);
            frameList[i] = frame;
        }
        var secFrameDataList = meta.optJSONArray("sec_frames");
        SecondaryFrame[] secFrameList = null;
        if (secFrameDataList != null) {
            assert(secFrameDataList.length() == numFrames);
            secFrameList = new SecondaryFrame[numFrames];
            var secFrameFolder = importFolder.resolve("sec_graphics");
            for (int i = 0; i < numFrames; i++) {
                var frame = SecondaryFrame.importFrame(secFrameDataList.getJSONObject(i), secFrameFolder, i);
                secFrameList[i] = frame;
            }
        }
        return new SLPFileVer4(header, frameList, secFrameList);
    }

    @Override
    public MemorySegment toNativeData(PaletteContainer palettes) {
        long requireSize = nativeByteSize();
        var data = MemorySegment.allocateNative(requireSize);
        long frameInfoOffset = roundUpMod16(Header.nativeByteSize());
        long currentOffset = frameInfoOffset;
        var version = this.header.version();
        int numFrames = this.header.numFrames();
        var frameInfoList = new FrameInfo[numFrames];
        currentOffset += numFrames * FrameInfo.nativeByteSize();
        for (int i = 0; i < numFrames; i++) {
            currentOffset = roundUpMod16(currentOffset);
            var frame = this.frameList[i];
            var info = frame.toNativeData(data, currentOffset, version, palettes);
            frameInfoList[i] = info.frameInfo();
            currentOffset = info.currentOffset();
        }
        long secFrameInfoOffset = 0;
        SecondaryFrameInfo[] secFrameInfoList = null;
        if (this.secFrameList.isPresent()) {
            var secFrameList = this.secFrameList.get();
            secFrameInfoOffset = roundUpMod32(currentOffset);
            currentOffset = secFrameInfoOffset;
            secFrameInfoList = new SecondaryFrameInfo[numFrames];
            currentOffset += SecondaryFrameInfo.nativeByteSize() * numFrames;
            for (int i = 0; i < numFrames; i++) {
                currentOffset = roundUpMod16(currentOffset);
                var frame = secFrameList[i];
                var info = frame.toNativeData(data, currentOffset);
                secFrameInfoList[i] = info.frameInfo();
                currentOffset = info.currentOffset();
            }
        }
        assert(currentOffset == requireSize);
        var header = this.header.builder()
                .offsetMain((int)frameInfoOffset)
                .offsetSecondary((int)secFrameInfoOffset)
                .build();
        header.toNativeData(data, 0L);
        currentOffset = frameInfoOffset;
        for (int i = 0; i < numFrames; i++) {
            frameInfoList[i].toNativeData(data, currentOffset);
            currentOffset += FrameInfo.nativeByteSize();
        }
        if (secFrameInfoOffset != 0) {
            currentOffset = secFrameInfoOffset;
            for (int i = 0; i < numFrames; i++) {
                secFrameInfoList[i].toNativeData(data, currentOffset);
                currentOffset += SecondaryFrameInfo.nativeByteSize();
            }
        }
        return data;
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
        if (this.secFrameList.isPresent()) {
            var secFrameList = this.secFrameList.get();
            var secFrameDataList = new JSONArray();
            meta.put("sec_frames", secFrameDataList);
            var secFrameFolder = exportFolder.resolve("sec_graphics");
            Files.createDirectories(secFrameFolder);
            for (int i = 0; i < numFrames; i++) {
                var secFrame = secFrameList[i];
                var secFrameData = secFrame.exportFrame(secFrameFolder, i);
                secFrameDataList.put(secFrameData);
            }
        }
        Files.writeString(exportFolder.resolve("meta.json"), meta.toString(4), StandardCharsets.UTF_8);
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
        if (this.secFrameList.isPresent()) {
            var secFrameList = this.secFrameList.get();
            result = roundUpMod32(result);
            result += numFrames * SecondaryFrameInfo.nativeByteSize();
            for (int i = 0; i < numFrames; i++) {
                var secFrame = secFrameList[i];
                result = roundUpMod16(result);
                result += secFrame.nativeByteSize();
            }
        }
        return result;
    }
}
