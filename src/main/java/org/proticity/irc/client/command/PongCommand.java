package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class PongCommand extends IrcCommand {
    public PongCommand(@NonNull CommandBuilder builder) {
        super(builder);
        assert builder.getTrailingParameter() != null;
    }

    @NonNull
    public String getHost() {
        return getTrailingParameter().get();
    }
}
