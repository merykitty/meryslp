package io.github.merykitty.slpprocessor.image;

public enum PxColourValueType {
    WITHOUT_MODIFIER,
    WITH_MODIFIER,
    RAW_COLOUR;

    public long nativeByteSize() {
        if (this == WITHOUT_MODIFIER) {
            return 1;
        } else if (this == WITH_MODIFIER) {
            return 2;
        } else {
            return 4;
        }
    }
}
