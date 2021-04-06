package io.github.merykitty.slpprocessor.command;

import io.github.merykitty.slpprocessor.image.*;
import io.github.merykitty.slpprocessor.misc.CommandResolveResult;
import io.github.merykitty.slpprocessor.misc.SecCommandResolveResult;
import io.github.merykitty.slpprocessor.misc.ubyte;
import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

import io.github.merykitty.slpprocessor.command.impl.*;

public class Commands {
    private static final byte MASK = 0x0f;
    public static final int FILL_THRESHOLD = 1 << 3;
    public static final int LESSER_THRESHOLD = 1 << 6;

    public static Command readCommand(MemorySegment data, long offset, PxColourValueType commandDataType, Palette palette) {
        byte cmdByte = MemoryAccess.getByteAtOffset(data, offset);
        byte cmd = (byte)(cmdByte & MASK);
        if (cmd == 0x00 || cmd == 0x04 || cmd == 0x08 || cmd == 0x0c) {
            return new LesserDraw(data, offset, commandDataType, palette);
        } else if (cmd == 0x01 || cmd == 0x05 || cmd == 0x09 || cmd == 0x0d) {
            return new LesserSkip(data, offset);
        } else if (cmd == 0x02) {
            return new GreaterDraw(data, offset, commandDataType, palette);
        } else if (cmd == 0x03) {
            return new GreaterSkip(data, offset);
        } else if (cmd == 0x06) {
            return new PlayerColourDraw(data, offset, commandDataType);
        } else if (cmd == 0x07) {
            return new Fill(data, offset, commandDataType, palette);
        } else if (cmd == 0x0a) {
            return new FillPlayerColour(data, offset, commandDataType);
        } else if (cmd == 0x0f) {
            return new EndOfRow();
        } else if (cmd == 0x0e) {
            if (cmdByte == 0x4e) {
                return new Outline(data, offset, commandDataType);
            } else if (cmdByte == 0x5e) {
                return new OutlineFill(data, offset, commandDataType);
            } else if (cmdByte == (byte)0x9e) {
                return new PremultipliedAlpha(data, offset, commandDataType, palette);
            }
        }
        throw new AssertionError("Command byte: " + Integer.toHexString(cmdByte));
    }

    public static SecondaryCommand readSecCommand(MemorySegment data, long offset, SecFrameType frameType) {
        byte cmdByte = MemoryAccess.getByteAtOffset(data, offset);
        byte cmd = (byte)(cmdByte & MASK);
        if (cmd == 0x00 || cmd == 0x04 || cmd == 0x08 || cmd == 0x0c) {
            return new SecLesserDraw(data, offset, frameType);
        } else if (cmd == 0x01 || cmd == 0x05 || cmd == 0x09 || cmd == 0x0d) {
            return new SecLesserSkip(data, offset);
        } else if (cmd == 0x02) {
            return new SecGreaterDraw(data, offset, frameType);
        } else if (cmd == 0x03) {
            return new SecGreaterSkip(data, offset);
        } else if (cmd == 0x07) {
            return new SecFill(data, offset, frameType);
        } else if (cmd == 0x0f) {
            return new SecEndOfRow();
        }
        throw new AssertionError();
    }

    public static CommandResolveResult importCommand(Artboard artboard, DrawCursor cursor) {
        int startX = cursor.x();
        int y = cursor.y();
        var type = PixelType.START;
        Command command;
        RawColour currentColour = RawColour.default;
        ubyte currentShade = ubyte.default;
        for(int x = startX, consecutive = 0;; x++) {
            if (x == artboard.width()) {
                if (type == PixelType.RAW) {
                    type = PixelType.RAW_DISTINCT;
                } else if (type == PixelType.PLAYER) {
                    type = PixelType.PLAYER_DISTINCT;
                }
                command = resolveCommand(artboard, startX, x, y, type);
                cursor = new DrawCursor(x, y);
                break;
            }
            var rawOptional = artboard.readRaw(x, y);
            var outlineOptional = artboard.readOutline(x, y);
            var playerOptional = artboard.readPlayer(x, y);
            if (outlineOptional.isPresent()) {
                assert (rawOptional.isEmpty() && playerOptional.isEmpty());
                var shade = outlineOptional.get();
                if (type == PixelType.START) {
                    type = PixelType.OUTLINE;
                    currentShade = shade;
                } else if (type == PixelType.OUTLINE) {
                    if (currentShade != shade) {
                        type = PixelType.OUTLINE_SINGLE;
                        command = resolveCommand(artboard, startX, x, y, type);
                        cursor = new DrawCursor(x, y);
                        break;
                    } else {
                        type = PixelType.OUTLINE_FILL;
                    }
                } else if (type == PixelType.OUTLINE_FILL) {
                    if (currentShade != shade) {
                        command = resolveCommand(artboard, startX, x, y, type);
                        cursor = new DrawCursor(x, y);
                        break;
                    }
                } else {
                    if (type == PixelType.RAW) {
                        type = PixelType.RAW_DISTINCT;
                    } else if (type == PixelType.PLAYER) {
                        type = PixelType.PLAYER_DISTINCT;
                    }
                    command = resolveCommand(artboard, startX, x, y, type);
                    cursor = new DrawCursor(x, y);
                    break;
                }
            } else if (playerOptional.isPresent()) {
                assert(rawOptional.isEmpty() && outlineOptional.isEmpty());
                ubyte shade = playerOptional.get();
                if (type == PixelType.START) {
                    type = PixelType.PLAYER;
                    consecutive = 1;
                    currentShade = shade;
                } else if (type == PixelType.PLAYER) {
                    if (shade == currentShade) {
                        consecutive++;
                        if (consecutive >= FILL_THRESHOLD) {
                            type = PixelType.PLAYER_FILL;
                        }
                    } else {
                        currentShade = shade;
                        consecutive = 1;
                        type = PixelType.PLAYER_DISTINCT;
                    }
                } else if (type == PixelType.PLAYER_DISTINCT) {
                    if (shade == currentShade) {
                        consecutive++;
                        if (consecutive >= FILL_THRESHOLD) {
                            command = resolveCommand(artboard, startX, x - FILL_THRESHOLD + 1, y, type);
                            cursor = new DrawCursor(x - FILL_THRESHOLD + 1, y);
                            break;
                        }
                    } else {
                        consecutive = 1;
                        currentShade = shade;
                    }
                } else if (type == PixelType.PLAYER_FILL) {
                    if (shade != currentShade) {
                        command = resolveCommand(artboard, startX, x, y, type);
                        cursor = new DrawCursor(x, y);
                        break;
                    }
                } else {
                    if (type == PixelType.RAW) {
                        type = PixelType.RAW_DISTINCT;
                    }
                    command = resolveCommand(artboard, startX, x, y, type);
                    cursor = new DrawCursor(x, y);
                    break;
                }
            } else if (rawOptional.isPresent()) {
                assert(outlineOptional.isEmpty() && playerOptional.isEmpty());
                var colour = rawOptional.get();
                if (colour.alpha() == ubyte.MAX_VALUE) {
                    if (type == PixelType.START) {
                        type = PixelType.RAW;
                        consecutive = 1;
                        currentColour = colour;
                    } else if (type == PixelType.RAW) {
                        if (colour == currentColour) {
                            consecutive++;
                            if (consecutive >= FILL_THRESHOLD) {
                                type = PixelType.RAW_FILL;
                            }
                        } else {
                            currentColour = colour;
                            consecutive = 1;
                            type = PixelType.RAW_DISTINCT;
                        }
                    } else if (type == PixelType.RAW_DISTINCT) {
                        if (colour == currentColour) {
                            consecutive++;
                            if (consecutive >= FILL_THRESHOLD) {
                                command = resolveCommand(artboard, startX, x - FILL_THRESHOLD + 1, y, type);
                                cursor = new DrawCursor(x - FILL_THRESHOLD + 1, y);
                                break;
                            }
                        } else {
                            consecutive = 1;
                            currentColour = colour;
                        }
                    } else if (type == PixelType.RAW_FILL) {
                        if (colour != currentColour) {
                            command = resolveCommand(artboard, startX, x, y, type);
                            cursor = new DrawCursor(x, y);
                            break;
                        }
                    } else {
                        if (type == PixelType.PLAYER) {
                            type = PixelType.PLAYER_DISTINCT;
                        }
                        command = resolveCommand(artboard, startX, x, y, type);
                        cursor = new DrawCursor(x, y);
                        break;
                    }
                } else {
                    if (type == PixelType.START) {
                        type = PixelType.TRANSPARENT;
                    } else if (type != PixelType.TRANSPARENT) {
                        if (type == PixelType.RAW) {
                            type = PixelType.RAW_DISTINCT;
                        } else if (type == PixelType.PLAYER) {
                            type = PixelType.PLAYER_DISTINCT;
                        }
                        command = resolveCommand(artboard, startX, x, y, type);
                        cursor = new DrawCursor(x, y);
                        break;
                    }
                }
            } else {
                if (type == PixelType.START) {
                    type = PixelType.SKIP;
                } else if (type != PixelType.SKIP) {
                    if (type == PixelType.RAW) {
                        type = PixelType.RAW_DISTINCT;
                    } else if (type == PixelType.PLAYER) {
                        type = PixelType.PLAYER_DISTINCT;
                    }
                    command = resolveCommand(artboard, startX, x, y, type);
                    cursor = new DrawCursor(x, y);
                    break;
                }
            }
        }
        return new CommandResolveResult(cursor, command);
    }

    public static SecCommandResolveResult importSecCommand(SecondaryArtboard artboard, DrawCursor cursor) {
        int startX = cursor.x();
        int y = cursor.y();
        var type = SecPixelType.START;
        SecondaryCommand command;
        ubyte currentShade = ubyte.default;
        for (int x = startX, consecutive = 0;; x++) {
            if (x == artboard.width()) {
                if (type == SecPixelType.MAIN) {
                    type = SecPixelType.MAIN_DISTINCT;
                }
                command = resolveSecCommand(artboard, startX, x, y, type);
                cursor = new DrawCursor(x, y);
                break;
            }
            var mainOptional = artboard.readMain(x, y);
            if (mainOptional.isPresent()) {
                var shade = mainOptional.get();
                if (type == SecPixelType.START) {
                    type = SecPixelType.MAIN;
                    consecutive = 1;
                    currentShade = shade;
                } else if (type == SecPixelType.MAIN) {
                    if (currentShade != shade) {
                        type = SecPixelType.MAIN_DISTINCT;
                        consecutive = 1;
                        currentShade = shade;
                    } else {
                        consecutive++;
                        if (consecutive >= FILL_THRESHOLD) {
                            type = SecPixelType.MAIN_FILL;
                        }
                    }
                } else if (type == SecPixelType.MAIN_DISTINCT) {
                    if (currentShade == shade) {
                        consecutive++;
                        if (consecutive >= FILL_THRESHOLD) {
                            command = resolveSecCommand(artboard, startX, x - FILL_THRESHOLD + 1, y, type);
                            cursor = new DrawCursor(x - FILL_THRESHOLD + 1, y);
                            break;
                        }
                    } else {
                        consecutive = 1;
                        currentShade = shade;
                    }
                } else if (type == SecPixelType.MAIN_FILL) {
                    if (currentShade != shade) {
                        command = resolveSecCommand(artboard, startX, x, y, type);
                        cursor = new DrawCursor(x, y);
                        break;
                    }
                } else {
                    command = resolveSecCommand(artboard, startX, x, y, type);
                    cursor = new DrawCursor(x, y);
                    break;
                }
            } else {
                if (type == SecPixelType.START) {
                    type = SecPixelType.SKIP;
                } else if (type != SecPixelType.SKIP) {
                    if (type == SecPixelType.MAIN) {
                        type = SecPixelType.MAIN_DISTINCT;
                    }
                    command = resolveSecCommand(artboard, startX, x, y, type);
                    cursor = new DrawCursor(x, y);
                    break;
                }
            }
        }
        return new SecCommandResolveResult(cursor, command);
    }

    private static Command resolveCommand(Artboard artboard, int startX, int endX, int y, PixelType type) {
        if (type == PixelType.RAW_DISTINCT) {
            if (endX - startX < LESSER_THRESHOLD) {
                return new LesserDraw(artboard, startX, endX, y);
            } else {
                return new GreaterDraw(artboard, startX, endX, y);
            }
        } else if (type == PixelType.RAW_FILL) {
            return new Fill(artboard, startX, endX, y);
        } else if (type == PixelType.OUTLINE_SINGLE) {
            return new Outline(artboard, startX, endX, y);
        } else if (type == PixelType.OUTLINE_FILL) {
            return new OutlineFill(artboard, startX, endX, y);
        } else if (type == PixelType.PLAYER_DISTINCT) {
            return new PlayerColourDraw(artboard, startX, endX, y);
        } else if (type == PixelType.PLAYER_FILL) {
            return new FillPlayerColour(artboard, startX, endX, y);
        } else if (type == PixelType.SKIP) {
            if (endX - startX < LESSER_THRESHOLD) {
                return new LesserSkip(artboard, startX, endX, y);
            } else {
                return new GreaterSkip(artboard, startX, endX, y);
            }
        } else if (type == PixelType.TRANSPARENT) {
            return new PremultipliedAlpha(artboard, startX, endX, y);
        } else {
            throw new AssertionError("Unexpected type: " + type.name());
        }
    }

    private static SecondaryCommand resolveSecCommand(SecondaryArtboard artboard, int startX, int endX, int y, SecPixelType type) {
        if (type == SecPixelType.MAIN_DISTINCT) {
            if (endX - startX < LESSER_THRESHOLD) {
                return new SecLesserDraw(artboard, startX, endX, y);
            } else {
                return new SecGreaterDraw(artboard, startX, endX, y);
            }
        } else if (type == SecPixelType.MAIN_FILL) {
            return new SecFill(artboard, startX, endX, y);
        } else if (type == SecPixelType.SKIP) {
            if (endX - startX < LESSER_THRESHOLD) {
                return new SecLesserSkip(artboard, startX, endX, y);
            } else {
                return new SecGreaterSkip(artboard, startX, endX, y);
            }
        } else {
            throw new AssertionError("Unexpected type: " + type.name());
        }
    }

    private enum PixelType {
        START,
        RAW,
        RAW_DISTINCT,
        RAW_FILL,
        OUTLINE,
        OUTLINE_SINGLE,
        OUTLINE_FILL,
        PLAYER,
        PLAYER_DISTINCT,
        PLAYER_FILL,
        SKIP,
        TRANSPARENT
    }

    private enum SecPixelType {
        START,
        MAIN,
        MAIN_DISTINCT,
        MAIN_FILL,
        SKIP
    }
}
