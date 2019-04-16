package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class PingCommand extends IrcCommand {
    public PingCommand(@NonNull CommandBuilder builder) {
        super(builder);
        assert builder.getTrailingParameter() != null;
    }

    @NonNull
    public String getHost() {
        return getTrailingParameter().get();
    }
}
