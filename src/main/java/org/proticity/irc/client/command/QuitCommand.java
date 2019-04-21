package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class QuitCommand extends IrcCommand {
    public QuitCommand(@NonNull CommandBuilder builder) {
        super(builder);
    }

    @NonNull
    public String getMessage() {
        return getTrailingParameter().get();
    }
}
