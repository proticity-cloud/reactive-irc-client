package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class ErrorCommand extends IrcCommand {
    public ErrorCommand(@NonNull CommandBuilder builder) {
        super(builder);
    }

    @NonNull
    public String getMessage() {
        return getTrailingParameter().get();
    }
}
