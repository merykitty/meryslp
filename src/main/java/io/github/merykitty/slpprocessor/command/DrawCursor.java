package io.github.merykitty.slpprocessor.command;

@__primitive__
public class DrawCursor {
    private int x;
    private int y;

    public DrawCursor(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }
}
