package io.github.merykitty.meryslp.common;

import io.github.merykitty.meryslp.command.Commands;
import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.command.SecondaryCommand;
import io.github.merykitty.meryslp.command.impl.SecEndOfRow;
import io.github.merykitty.meryslp.image.SecFrameType;
import io.github.merykitty.meryslp.image.SecondaryArtboard;
import io.github.merykitty.meryslp.misc.SecFrameSerializeRecord;
import io.github.merykitty.meryslp.misc.ubyte;
import io.github.merykitty.meryslp.misc.uint;
import io.github.merykitty.meryslp.misc.ushort;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static io.github.merykitty.meryslp.common.SLPFiles.roundUpMod16;

@__primitive__
public class SecondaryFrame {
    private static final int EMPTY_SIGNAL = 0x8000;

    private SecondaryFrameInfo frameInfo;
    private FrameRowEdge[] frameRowEdgeList;
    private List<SecondaryCommand> commandList;

    private SecondaryFrame(SecondaryFrameInfo frameInfo, FrameRowEdge[] frameRowEdgeList, List<SecondaryCommand> commandList) {
        this.frameInfo = frameInfo;
        this.frameRowEdgeList = frameRowEdgeList;
        this.commandList = commandList;
    }

    public static SecondaryFrame ofNativeData(MemorySegment data, SecondaryFrameInfo frameInfo, long nextFrameOffset) {
        int height = frameInfo.height();
        long currentOffset = frameInfo.outlineTableOffset();
        assert((currentOffset & 0x0f) == 0);
        var frameRowEdgeList = new FrameRowEdge[height];
        for (int i = 0; i < height; i++) {
            var frameRowEdge = FrameRowEdge.ofNativeData(data, currentOffset);
            frameRowEdgeList[i] = frameRowEdge;
            currentOffset += frameRowEdge.nativeByteSize();
        }
        for (long i = currentOffset; i < frameInfo.cmdTableOffset(); i++) {
            assert(MemoryAccess.getByteAtOffset(data, i) == 0);
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
        for (long i = currentOffset; i < nextFrameOffset; i++) {
            assert(MemoryAccess.getByteAtOffset(data, i) == 0);
        }
        return new SecondaryFrame(frameInfo, frameRowEdgeList, commandList);
    }

    public static SecondaryFrame importFrame(JSONObject frameData, Path imageFolder, int frameNum) throws IOException {
        try (var artboard = SecondaryArtboard.importFrame(imageFolder, frameNum)) {
            int nul = frameData.getInt("sec_null");
            int properties = frameData.getInt("sec_properties");
            int frameType = frameData.getInt("sec_frame_type");
            int width = artboard.width();
            int height = artboard.height();
            int hotspotX = frameData.getInt("sec_hotspot_x");
            int hotspotY = frameData.getInt("sec_hotspot_y");
            var frameInfo = SecondaryFrameInfo.emptyBuilder()
                    .nul(nul)
                    .properties(properties)
                    .frameType(new ubyte((byte)frameType))
                    .width(width)
                    .height(height)
                    .hotspotX(hotspotX)
                    .hotspotY(hotspotY)
                    .build();
            var rowEdgeList = new FrameRowEdge[height];
            var commandList = new ArrayList<SecondaryCommand>();
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
            return new SecondaryFrame(frameInfo, rowEdgeList, commandList);
        }
    }

    public JSONObject exportFrame(Path imageFolder, int frameNum) throws IOException {
        int width = this.frameInfo.width();
        int height = this.frameInfo.height();

        var frameData = new JSONObject();
        frameData.put("sec_null", this.frameInfo.nul());
        frameData.put("sec_properties", this.frameInfo.properties());
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
                        assert (i == 0 || this.commandList.get(i - 1) instanceof SecEndOfRow);
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
        long commandOffsetOffset = roundUpMod16(currentOffset);
        currentOffset = commandOffsetOffset;
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
                .cmdTableOffset(new uint((int)commandOffsetOffset))
                .outlineTableOffset(new uint((int)outlineOffset))
                .build();
        currentOffset = commandOffsetOffset;
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
//                e.printStackTrace();
            }
            return SecFrameType.VFX_ALPHA;
        }
    }
}
