package io.github.merykitty.slpprocessor.common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.merykitty.slpprocessor.misc.FrameSerializeRecord;
import io.github.merykitty.slpprocessor.misc.uint;
import io.github.merykitty.slpprocessor.misc.ushort;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.Commands;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.image.Artboard;
import io.github.merykitty.slpprocessor.image.PaletteContainer;
import io.github.merykitty.slpprocessor.image.PxColourValueType;
import io.github.merykitty.slpprocessor.command.impl.EndOfRow;

import static io.github.merykitty.slpprocessor.common.SLPFiles.roundUpMod16;

@__primitive__
public class Frame {
    private static final int EMPTY_SIGNAL = 0x8000;

    private FrameInfo frameInfo;
    private FrameRowEdge[] frameRowEdgeList;
    private List<Command> commandList;

    private Frame(FrameInfo frameInfo, FrameRowEdge[] frameRowEdgeList, List<Command> commandList) {
        this.frameInfo = frameInfo;
        this.frameRowEdgeList = frameRowEdgeList;
        this.commandList = commandList;
    }

    public static Frame ofNativeData(MemorySegment data, FrameInfo frameInfo, long nextFrameOffset, Version version, PaletteContainer palettes) {
        int height = frameInfo.height();
        long currentOffset = frameInfo.outlineTableOffset();
        assert((currentOffset & 0x0f) == 0) : ("Misaligned offset: " + currentOffset);
        var frameRowEdgeList = new FrameRowEdge[height];
        for (int i = 0; i < height; i++) {
            var frameRowEdge = FrameRowEdge.ofNativeData(data, currentOffset);
            frameRowEdgeList[i] = frameRowEdge;
            currentOffset += FrameRowEdge.nativeByteSize();
        }
        for (long i = currentOffset; i < frameInfo.cmdTableOffset(); i++) {
            assert(MemoryAccess.getByteAtOffset(data, i) == 0);
        }
        currentOffset = frameInfo.cmdTableOffset();
        assert((currentOffset & 0x0f) == 0) : ("Misaligned offset: " + currentOffset);
        var commandOffsetList = new CommandOffset[height];
        for (int i = 0; i < height; i++) {
            var commandOffset = CommandOffset.ofNativeData(data, currentOffset);
            commandOffsetList[i] = commandOffset;
            currentOffset += CommandOffset.nativeByteSize();
        }
        var commandList = new ArrayList<Command>();
        int paletteId = frameInfo.properties();
        var commandDataType = commandDataType(version, paletteId);
        assert(currentOffset == commandOffsetList[0].offset()) : ("Expected offset: " + commandOffsetList[0].offset() + ", actual offset: " + currentOffset);
        var palette = palettes.get(paletteId);
        for (int row = 0;; ) {
            var command = Commands.readCommand(data, currentOffset, commandDataType, palette);
            commandList.add(command);
            currentOffset += command.nativeByteSize(commandDataType);
            if (command instanceof EndOfRow) {
                row++;
                if (row < height) {
                    assert(currentOffset == commandOffsetList[row].offset()) : ("Command misaligned, expected offset: " + commandOffsetList[row].offset() + ", actual offset: " + currentOffset);
                } else {
                    break;
                }
            }
        }
        for (long i = currentOffset; i < nextFrameOffset; i++) {
            assert(MemoryAccess.getByteAtOffset(data, i) == 0);
        }
        return new Frame(frameInfo, frameRowEdgeList, commandList);
    }

    public static Frame importFrame(JSONObject frameData, Path imageFolder, int frameNum) throws IOException {
        try (var artboard = Artboard.importFrame(imageFolder, frameNum)) {
            int properties = frameData.getInt("properties");
            int width = artboard.width();
            int height = artboard.height();
            int hotspotX = frameData.getInt("hotspot_x");
            int hotspotY = frameData.getInt("hotspot_y");
            var frameInfo = FrameInfo.emptyBuilder()
                    .properties(properties)
                    .width(width)
                    .height(height)
                    .hotspotX(hotspotX)
                    .hotspotY(hotspotY)
                    .build();
            var rowEdgeList = new FrameRowEdge[height];
            var commandList = new ArrayList<Command>();
            for (int y = 0; y < height; y++) {
                int leftEdge;
                int rightEdge;
                int rightEdgeCoordinate;
                for (int x = 0;; x++) {
                    if (x == width) {
                        leftEdge = 0x8000;
                        break;
                    } else if (artboard.readRaw(x, y).isPresent() || artboard.readOutline(x, y).isPresent() || artboard.readPlayer(x, y).isPresent()) {
                        leftEdge = x;
                        break;
                    }
                }
                if (leftEdge != 0x8000) {
                    for (int x = width - 1;; x--) {
                        assert(x >= leftEdge);
                        if (artboard.readRaw(x, y).isPresent() || artboard.readOutline(x, y).isPresent() || artboard.readPlayer(x, y).isPresent()) {
                            rightEdge = width - 1 - x;
                            rightEdgeCoordinate = x + 1;
                            break;
                        }
                    }
                    var drawCursor = new DrawCursor(leftEdge, y);
                    while (true) {
                        var result = Commands.importCommand(artboard, drawCursor);
                        commandList.add(result.command());
                        drawCursor = result.cursor();
                        if (drawCursor.x() == rightEdgeCoordinate) {
                            break;
                        }
                        assert(drawCursor.x() <= rightEdgeCoordinate);
                    }
                } else {
                    rightEdge = 0;
                }
                commandList.add(new EndOfRow());
                assert(leftEdge <= ushort.MAX_VALUE.value() && rightEdge < ushort.MAX_VALUE.value());
                rowEdgeList[y] = new FrameRowEdge(new ushort((short)leftEdge), new ushort ((short)rightEdge));
            }
            return new Frame(frameInfo, rowEdgeList, commandList);
        }
    }

    public JSONObject exportFrame(Path imageFolder, int frameNum) throws IOException {
        int width = this.frameInfo.width();
        int height = this.frameInfo.height();

        var frameData = new JSONObject();
        frameData.put("properties", this.frameInfo.properties());
        frameData.put("hotspot_x", this.frameInfo.hotspotX());
        frameData.put("hotspot_y", this.frameInfo.hotspotY());

        try (var artboard = Artboard.createFrame(width, height);) {
            int y = 0;
            var drawCursor = new DrawCursor(this.frameRowEdgeList[0].leftSpace(), y);
            for (int i = 0; i < this.commandList.size(); i++) {
                var command = this.commandList.get(i);
                if (command instanceof EndOfRow) {
                    if (this.frameRowEdgeList[y].leftSpace() != EMPTY_SIGNAL && this.frameRowEdgeList[y].rightSpace() != EMPTY_SIGNAL) {
                        assert (drawCursor.x() == width - this.frameRowEdgeList[y].rightSpace()) : ("Expected cursor x = " + (width - this.frameRowEdgeList[y].rightSpace()) + ", actual cursor x = " + drawCursor.x());
                    } else {
                        assert (i == 0 || this.commandList.get(i - 1) instanceof EndOfRow);
                    }
                    y++;
                    if (y < height) {
                        drawCursor = new DrawCursor(this.frameRowEdgeList[y].leftSpace(), y);
                    }
                } else {
                    drawCursor = command.draw(artboard, drawCursor);
                }
            }
            assert (y == height);
            artboard.exportFrame(imageFolder, frameNum);
        }
        return frameData;
    }

    public FrameSerializeRecord toNativeData(MemorySegment data, long offset, Version version, PaletteContainer palettes) {
        long outlineOffset = offset;
        long currentOffset = outlineOffset;
        int height = this.frameInfo.height();
        for (int i = 0; i < height; i++) {
            this.frameRowEdgeList[i].toNativeData(data, currentOffset);
            currentOffset += FrameRowEdge.nativeByteSize();
        }
        long commandOffsetOffset = roundUpMod16(currentOffset);
        currentOffset = commandOffsetOffset;
        for (int i = 0; i < height; i++) {
            currentOffset += CommandOffset.nativeByteSize();
        }
        var commandOffsetList = new CommandOffset[height];
        var palette = palettes.get(this.frameInfo.properties());
        var commandDataType = commandDataType(version, this.frameInfo.properties());
        int row = 0;
        commandOffsetList[row] = new CommandOffset(new uint((int) currentOffset));
        for (var command : this.commandList) {
            command.toNative(data, currentOffset, commandDataType, palette);
            currentOffset += command.nativeByteSize(commandDataType);
            if (command instanceof EndOfRow) {
                row++;
                if (row < height) {
                    commandOffsetList[row] = new CommandOffset(new uint((int)currentOffset));
                }
            }
        }
        long finalOffset = currentOffset;
        var frameInfo = this.frameInfo.builder()
                .cmdTableOffset(new uint((int)commandOffsetOffset))
                .outlineTableOffset(new uint((int)outlineOffset))
                .build();
        currentOffset = commandOffsetOffset;
        for (int i = 0; i < height; i++) {
            commandOffsetList[i].toNativeData(data, currentOffset);
            currentOffset += CommandOffset.nativeByteSize();
        }
        return new FrameSerializeRecord(finalOffset, frameInfo);
    }

    public long nativeByteSize(Version version) {
        int height = this.frameInfo.height();
        long result = 0;
        result += height * FrameRowEdge.nativeByteSize();
        result = roundUpMod16(result);
        result += height * CommandOffset.nativeByteSize();
        var commandDataType = commandDataType(version, this.frameInfo.properties());
        for (var command : this.commandList) {
            result += command.nativeByteSize(commandDataType);
        }
        return result;
    }

    private static PxColourValueType commandDataType(Version version, int properties) {
        PxColourValueType commandDataType;
        if ((properties & 0x07) == 0x07) {
            commandDataType = PxColourValueType.RAW_COLOUR;
        } else {
            if (version.lessThan(Version.of("4.1a"))) {
                commandDataType = PxColourValueType.WITHOUT_MODIFIER;
            } else {
                commandDataType = PxColourValueType.WITH_MODIFIER;
            }
        }
        return commandDataType;
    }
}
