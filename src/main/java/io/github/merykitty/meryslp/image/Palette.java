package io.github.merykitty.meryslp.image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import io.github.merykitty.meryslp.misc.*;

@__primitive__
public class Palette {
    private static final String HEADER = "JASC-PAL";
    private static final String VERSION = "0100";
    private static final String LENGTH = "256";

    private RawColour[] colourList;

    public Palette(Path file) throws IOException {
        var lines = Files.readAllLines(file);
        assert(lines.get(0).equals(HEADER));
        assert(lines.get(1).equals(VERSION));
        assert(lines.get(2).equals(LENGTH));
        var tempList = new ArrayList<RawColour.ref>();
        for (int i = 3; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.startsWith("#")) {
                continue;
            }
            String[] lineParts = line.split(" ");
            ubyte red = new ubyte((byte)Integer.parseInt(lineParts[0]));
            ubyte green = new ubyte((byte)Integer.parseInt(lineParts[1]));
            ubyte blue = new ubyte((byte)Integer.parseInt(lineParts[2]));
            tempList.add(new RawColour(red, green, blue));
        }
        this.colourList = new RawColour[tempList.size()];
        for (int i = 0; i < tempList.size(); i++) {
            colourList[i] = tempList.get(i);
        }
    }

    public RawColour colour(ubyte index) {
        return this.colourList[index.value()];
    }

    public ubyte index(RawColour colour) {
        int nearest = 0;
        int minDistance = RawColour.squaredDistance(colour, this.colourList[nearest]);
        for (int i = 1; i < this.colourList.length; i++) {
            var temp = this.colourList[i];
            int tempDistance = RawColour.squaredDistance(temp, colour);
            if (tempDistance < minDistance) {
                minDistance = tempDistance;
                nearest = i;
            }
        }
        return new ubyte((byte)nearest);
    }

    public boolean isEmpty() {
        return this.colourList == null;
    }
}
