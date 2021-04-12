package io.github.merykitty.meryslp.misc;

import io.github.merykitty.meryslp.command.DrawCursor;
import io.github.merykitty.meryslp.command.SecondaryCommand;

@__primitive__
public class SecCommandResolveResult {
    DrawCursor cursor;
    SecondaryCommand command;

    public SecCommandResolveResult(DrawCursor cursor, SecondaryCommand command) {
        this.command = command;
        this.cursor = cursor;
    }

    public DrawCursor cursor() {
        return this.cursor;
    }

    public SecondaryCommand command() {
        return this.command;
    }
}
