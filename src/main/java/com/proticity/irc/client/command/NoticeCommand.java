package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class NoticeCommand<T> extends MessageCommand<T> {
    public NoticeCommand(@NonNull CommandBuilder builder, @NonNull T target) {
        super(builder, target);
    }
}
