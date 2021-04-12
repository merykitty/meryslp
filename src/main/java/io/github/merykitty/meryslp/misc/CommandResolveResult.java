package io.github.merykitty.meryslp.misc;

import io.github.merykitty.meryslp.command.Command;
import io.github.merykitty.meryslp.command.DrawCursor;

@__primitive__
public class CommandResolveResult {
    DrawCursor cursor;
    Command command;

    public CommandResolveResult(DrawCursor cursor, Command command) {
        this.command = command;
        this.cursor = cursor;
    }

    public DrawCursor cursor() {
        return this.cursor;
    }

    public Command command() {
        return this.command;
    }
}
