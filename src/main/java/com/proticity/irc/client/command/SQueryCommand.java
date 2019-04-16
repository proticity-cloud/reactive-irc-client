package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

import java.util.Objects;

public class SQueryCommand extends MessageCommand<User> {
    public SQueryCommand(@NonNull CommandBuilder builder) {
        super(builder, new User(builder.getParameter(0)));
    }
}
