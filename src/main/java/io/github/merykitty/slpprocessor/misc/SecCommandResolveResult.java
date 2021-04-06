package io.github.merykitty.slpprocessor.misc;

import io.github.merykitty.slpprocessor.command.Command;
import io.github.merykitty.slpprocessor.command.DrawCursor;
import io.github.merykitty.slpprocessor.command.SecondaryCommand;

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
