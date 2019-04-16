package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

import java.util.regex.Pattern;

public class PrivmsgCommand<T> extends MessageCommand<T> {
    private static final Pattern ACTION = Pattern.compile("^\u0001ACTION (?<action>[^\u0001]*)\u0001$");

    private String action;

    public PrivmsgCommand(@NonNull CommandBuilder builder, @NonNull T target) {
        super(builder, target);
        var matcher = ACTION.matcher(super.getMessage());
        if (matcher.lookingAt()) {
            action = matcher.group("action");
        }
    }

    public boolean isAction() {
        return action != null;
    }

    @NonNull
    @Override
    public String getMessage() {
        return isAction() ? action : super.getMessage();
    }
}
