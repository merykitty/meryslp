package io.github.merykitty.slpprocessor.misc;

import jdk.incubator.foreign.MemorySegment;

import java.io.IOException;
import java.nio.file.Path;

@__primitive__
public class Image {
    private int width;
    private int height;
    private MemorySegment data;

    public Image(int width, int height, MemorySegment data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public MemorySegment data() {
        return this.data;
    }
}
