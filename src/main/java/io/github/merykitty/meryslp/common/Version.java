package io.github.merykitty.meryslp.common;

import java.nio.charset.StandardCharsets;

import jdk.incubator.foreign.MemorySegment;

@__primitive__
public class Version {
    private byte major;
    private byte minor;
    private char release;

    public Version(byte major, byte minor, char release) {
        this.major = major;
        this.minor = minor;
        this.release = release;
    }

    public static Version of(String versionString) {
        assert(versionString.length() == 4) : ("Unexpected version length: " + versionString.length());
        byte major = (byte)(versionString.charAt(0) - '0');
        byte minor = (byte)(versionString.charAt(2) - '0');
        char release = versionString.charAt(3);
        return new Version(major, minor, release);
    }

    public static Version ofNativeData(MemorySegment versionSegment) {
        return of(new String(versionSegment.toByteArray(), StandardCharsets.US_ASCII).toLowerCase());
    }

    public int major() {
        return this.major;
    }

    public int minor() {
        return this.minor;
    }

    public char release() {
        return this.release;
    }

    public boolean lessThan(Version other) {
        if (this.major < other.major) {
            return true;
        } else if (this.major > other.major) {
            return false;
        } else {
            if (this.minor < other.minor) {
                return true;
            } else if (this.minor > other.minor) {
                return false;
            } else {
                return this.release < other.release;
            }
        }
    }

    @Override
    public String toString() {
        return this.major + "." + this.minor + this.release;
    }
}
