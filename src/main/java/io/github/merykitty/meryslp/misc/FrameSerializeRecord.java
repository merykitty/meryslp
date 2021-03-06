package io.github.merykitty.meryslp.misc;

import io.github.merykitty.meryslp.common.FrameInfo;

@__primitive__
public class FrameSerializeRecord {
    long currentOffset;
    FrameInfo frameInfo;

    public FrameSerializeRecord(long currentOffset, FrameInfo frameInfo) {
        this.currentOffset = currentOffset;
        this.frameInfo = frameInfo;
    }

    public long currentOffset() {
        return this.currentOffset;
    }

    public FrameInfo frameInfo() {
        return this.frameInfo;
    }
}
