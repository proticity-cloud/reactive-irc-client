package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class SQueryCommand extends MessageCommand<User> {
    public SQueryCommand(@NonNull CommandBuilder builder) {
        super(builder, new User(builder.getParameter(0)));
    }
}
