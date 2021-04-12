package io.github.merykitty.meryslp.misc;

@__primitive__
public class ubyte {
    public static final ubyte MAX_VALUE = new ubyte((byte)-1);

    private byte internal;

    public ubyte(byte value) {
        this.internal = value;
    }

    public short value() {
        return (short)(internal & 0xff);
    }

    public byte signed() {
        return this.internal;
    }

    @Override
    public String toString() {
        return Short.toString(this.value());
    }
}
