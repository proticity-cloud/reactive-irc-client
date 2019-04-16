package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

import java.util.Objects;

public class NickCommand extends IrcCommand {
    public NickCommand(@NonNull CommandBuilder builder) {
        super(builder);
    }

    @NonNull
    public String getNickname() {
        return Objects.requireNonNull(getParameters().get(0));
    }
}
