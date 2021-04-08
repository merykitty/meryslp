package io.github.merykitty.slpprocessor.image;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.json.JSONObject;
import org.json.JSONTokener;

@__primitive__
public class PaletteContainer {
    private int capacity;
    private Entry[] entryList;

    public PaletteContainer(Path paletteConfFile, Path paletteFolder) throws IOException {
        var confLines = Files.readAllLines(paletteConfFile, StandardCharsets.UTF_8);
        int capacity = confLines.size() * 2;
        var entryList = new Entry[capacity];
        String confString = String.join("\n", confLines);
        var tokeniser = new JSONTokener(confString);
        var confJson = new JSONObject(tokeniser);

        for (var keyIter = confJson.keys(); keyIter.hasNext(); ) {
            String keyString = keyIter.next();
            int key = Integer.decode(keyString);
            String value = confJson.getString(keyString);
            int bucket = key % capacity;
            var palette = new Palette(Path.of(paletteFolder.toString(), value));
            while (true) {
                if (entryList[bucket].value.isEmpty()) {
                    entryList[bucket] = new Entry(key, palette);
                    break;
                } else {
                    bucket++;
                    if (bucket == capacity) {
                        bucket = 0;
                    }
                }
            }
        }

        this.capacity = capacity;
        this.entryList = entryList;
    }

    public Palette get(int paletteId) {
        int bucket = paletteId % this.capacity;
        while (true) {
            var entry = this.entryList[bucket];
            if (entry.value.isEmpty()) {
                assert((paletteId & 0x07) == 0x07) : ("Unexpected palette: " + paletteId);
                return Palette.default;
            } else {
                if (entry.key == paletteId) {
                    return entry.value;
                } else {
                    bucket++;
                    if (bucket == this.capacity) {
                        bucket = 0;
                    }
                }
            }
        }
    }

    @__primitive__
    private static class Entry {
        int key;
        Palette value;

        public Entry(int key, Palette value) {
            this.key = key;
            this.value = value;
        }
    }
}
