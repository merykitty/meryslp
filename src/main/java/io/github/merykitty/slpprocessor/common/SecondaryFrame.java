package io.github.merykitty.slpprocessor.common;

import io.github.merykitty.slpprocessor.command.Commands;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.command.SecondaryCommand;
import io.github.merykitty.slpprocessor.command.impl.EndOfRow;
import io.github.merykitty.slpprocessor.command.impl.SecEndOfRow;
import io.github.merykitty.slpprocessor.image.SecFrameType;
import io.github.merykitty.slpprocessor.image.SecondaryArtboard;
import io.github.merykitty.slpprocessor.misc.*;
import jdk.incubator.foreign.MemorySegment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.merykitty.slpprocessor.common.SLPFiles.roundUpMod16;

@__primitive__
public class SecondaryFrame {
    private static final int EMPTY_SIGNAL = 0x8000;

    private SecondaryFrameInfo frameInfo;
    private FrameRowEdge[] frameRowEdgeList;
    private CommandOffset[] commandOffsetList;
    private List<SecondaryCommand> commandList;

    private SecondaryFrame(SecondaryFrameInfo frameInfo, FrameRowEdge[] frameRowEdgeList, CommandOffset[] commandOffsetList, List<SecondaryCommand> commandList) {
        this.frameInfo = frameInfo;
        this.frameRowEdgeList = frameRowEdgeList;
        this.commandOffsetList = commandOffsetList;
        this.commandList = commandList;
    }

    public static SecondaryFrame ofNativeData(MemorySegment data, SecondaryFrameInfo frameInfo) {
        int height = frameInfo.height();
        long currentOffset = frameInfo.outlineTableOffset();
        assert((currentOffset & 0x0f) == 0);
        var frameRowEdgeList = new FrameRowEdge[height];
        for (int i = 0; i < height; i++) {
            var frameRowEdge = FrameRowEdge.ofNativeData(data, currentOffset);
            frameRowEdgeList[i] = frameRowEdge;
            currentOffset += frameRowEdge.nativeByteSize();
        }
        currentOffset = frameInfo.cmdTableOffset();
        assert((currentOffset & 0x0f) == 0);
        var commandOffsetList = new CommandOffset[height];
        for (int i = 0; i < height; i++) {
            var commandOffset = CommandOffset.ofNativeData(data, currentOffset);
            commandOffsetList[i] = commandOffset;
            currentOffset += commandOffset.nativeByteSize();
        }
        var commandList = new ArrayList<SecondaryCommand>();
        var frameType = commandType(frameInfo.frameType());
        assert(currentOffset == commandOffsetList[0].offset());
        for (int row = 0;;) {
            var command = Commands.readSecCommand(data, currentOffset, frameType);
            commandList.add(command);
            currentOffset += command.nativeByteSize();
            if (command instanceof SecEndOfRow) {
                row++;
                if (row < height) {
                    assert (currentOffset == commandOffsetList[row].offset());
                } else {
                    break;
                }
            }
        }
        return new SecondaryFrame(frameInfo, frameRowEdgeList, commandOffsetList, commandList);
    }

    public static SecondaryFrame importFrame(JSONObject frameData, Path imageFolder, int frameNum) throws IOException {
        try (var artboard = SecondaryArtboard.importFrame(imageFolder, frameNum)) {
            int width = artboard.width();
            int height = artboard.height();
            int frameType = frameData.getInt("sec_frame_type");
            int hotspotX = frameData.getInt("sec_hotspot_x");
            int hotspotY = frameData.getInt("sec_hotspot_y");
            var rowEdgeList = new FrameRowEdge[height];
            var commandOffsetList = new CommandOffset[height];
            var commandList = new ArrayList<SecondaryCommand>();
            var frameInfo = SecondaryFrameInfo.emptyBuilder()
                    .width(width)
                    .height(height)
                    .frameType(new ubyte((byte)frameType))
                    .hotspotX(hotspotX)
                    .hotspotY(hotspotY)
                    .build();
            for (int y = 0; y < height; y++) {
                int leftEdge;
                int rightEdge;
                int rightEdgeCoordinate;
                for (int x = 0;; x++) {
                    if (x == width) {
                        leftEdge = 0x8000;
                        break;
                    } else if (artboard.readMain(x, y).isPresent()) {
                        leftEdge = x;
                        break;
                    }
                }
                if (leftEdge != 0x8000) {
                    for (int x = width - 1;; x--) {
                        assert(x >= leftEdge);
                        if (artboard.readMain(x, y).isPresent()) {
                            rightEdge = width - 1 - x;
                            rightEdgeCoordinate = x + 1;
                            break;
                        }
                    }
                    var drawCursor = new DrawCursor(leftEdge, y);
                    while (true) {
                        var result = Commands.importSecCommand(artboard, drawCursor);
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
                commandList.add(new SecEndOfRow());
                assert(leftEdge <= ushort.MAX_VALUE.value() && rightEdge < ushort.MAX_VALUE.value());
                rowEdgeList[y] = new FrameRowEdge(new ushort((short)leftEdge), new ushort ((short)rightEdge));
            }
            return new SecondaryFrame(frameInfo, rowEdgeList, commandOffsetList, commandList);
        }
    }

    public JSONObject exportFrame(Path imageFolder, int frameNum) throws IOException {
        int width = this.frameInfo.width();
        int height = this.frameInfo.height();

        var frameData = new JSONObject();
        frameData.put("sec_frame_type", this.frameInfo.frameType());
        frameData.put("sec_hotspot_x", this.frameInfo.hotspotX());
        frameData.put("sec_hotspot_y", this.frameInfo.hotspotY());

        try (var artboard = SecondaryArtboard.createFrame(width, height)) {
            int y = 0;
            var drawCursor = new DrawCursor(this.frameRowEdgeList[0].leftSpace(), y);
            for (int i = 0; i < this.commandList.size(); i++) {
                var command = this.commandList.get(i);
                if (command instanceof SecEndOfRow) {
                    if (this.frameRowEdgeList[y].leftSpace() != EMPTY_SIGNAL && this.frameRowEdgeList[y].rightSpace() != EMPTY_SIGNAL) {
                        assert (drawCursor.x() == width - this.frameRowEdgeList[y].rightSpace()) : ("Expected cursor x = " + (width - this.frameRowEdgeList[y].rightSpace()) + ", actual cursor x = " + drawCursor.x());
                    } else {
                        assert (this.commandList.get(i - 1) instanceof SecEndOfRow);
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

    public SecFrameSerializeRecord toNativeData(MemorySegment data, long offset) {
        long outlineOffset = offset;
        long currentOffset = outlineOffset;
        int height = this.frameInfo.height();
        for (int i = 0; i < height; i++) {
            this.frameRowEdgeList[i].toNativeData(data, currentOffset);
            currentOffset += FrameRowEdge.nativeByteSize();
        }
        long commandOffset = roundUpMod16(currentOffset);
        currentOffset = commandOffset;
        for (int i = 0; i < height; i++) {
            currentOffset += CommandOffset.nativeByteSize();
        }
        var commandOffsetList = new CommandOffset[height];
        var type = commandType(this.frameInfo.frameType());
        int row = 0;
        commandOffsetList[row] = new CommandOffset(new uint((int) currentOffset));
        for (var command : this.commandList) {
            command.toNative(data, currentOffset, type);
            currentOffset += command.nativeByteSize();
            if (command instanceof SecEndOfRow) {
                row++;
                if (row < height) {
                    commandOffsetList[row] = new CommandOffset(new uint((int)currentOffset));
                }
            }
        }
        long finalOffset = currentOffset;
        var frameInfo = this.frameInfo.builder()
                .cmdTableOffset(new uint((int)commandOffset))
                .outlineTableOffset(new uint((int)outlineOffset))
                .build();
        currentOffset = commandOffset;
        for (int i = 0; i < height; i++) {
            commandOffsetList[i].toNativeData(data, currentOffset);
            currentOffset += CommandOffset.nativeByteSize();
        }
        return new SecFrameSerializeRecord(finalOffset, frameInfo);
    }

    public long nativeByteSize() {
        int height = this.frameInfo.height();
        long result = 0;
        result += height * FrameRowEdge.nativeByteSize();
        result = roundUpMod16(result);
        result += height * CommandOffset.nativeByteSize();
        for (var command : this.commandList) {
            result += command.nativeByteSize();
        }
        return result;
    }

    private static SecFrameType commandType(int type) {
        if (type == 0x02) {
            return SecFrameType.SHADOW;
        } else if (type == 0x08) {
            return SecFrameType.VFX_ALPHA;
        } else {
            try {
                throw new IllegalArgumentException("Unrecognized type: 0x" + Integer.toHexString(type));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return SecFrameType.VFX_ALPHA;
        }
    }
}
