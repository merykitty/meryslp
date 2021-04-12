package io.github.merykitty.meryslp.misc;

@__primitive__
public class ushort {
    public static ushort MAX_VALUE = new ushort((short)-1);

    private short internal;

    public ushort(short value) {
        this.internal = value;
    }

    public int value() {
        return (int)internal & 0xffff;
    }

    public short signed() {
        return this.internal;
    }

    @Override
    public String toString() {
        return Integer.toString(this.value());
    }
}
