package io.github.merykitty.slpprocessor.misc;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.DrawCursor;

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
