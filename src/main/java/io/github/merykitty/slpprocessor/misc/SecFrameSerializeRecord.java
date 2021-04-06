package io.github.merykitty.slpprocessor.misc;

import io.github.merykitty.slpprocessor.common.SecondaryFrameInfo;

@__primitive__
public class SecFrameSerializeRecord {
    private long currentOffset;
    private SecondaryFrameInfo frameInfo;

    public SecFrameSerializeRecord(long currentOffset, SecondaryFrameInfo frameInfo) {
        this.currentOffset = currentOffset;
        this.frameInfo = frameInfo;
    }

    public long currentOffset() {
        return this.currentOffset;
    }

    public SecondaryFrameInfo frameInfo() {
        return this.frameInfo;
    }
}
