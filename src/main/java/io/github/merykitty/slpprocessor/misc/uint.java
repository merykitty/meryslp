package io.github.merykitty.slpprocessor.misc;

@__primitive__
public class uint {
    private int internal;

    public uint(int value) {
        this.internal = value;
    }

    public long value() {
        return (long)internal & 0xffffffff;
    }

    public int signed() {
        return this.internal;
    }

    @Override
    public String toString() {
        return Long.toString(this.value());
    }
}
