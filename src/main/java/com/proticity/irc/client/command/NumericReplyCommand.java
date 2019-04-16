package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class NumericReplyCommand extends IrcCommand {
    private int replyCode;

    public NumericReplyCommand(@NonNull CommandBuilder builder) {
        super(builder);
        replyCode = Integer.parseInt(builder.getCommand());
    }

    public int getReplyCode() {
        return replyCode;
    }
}
