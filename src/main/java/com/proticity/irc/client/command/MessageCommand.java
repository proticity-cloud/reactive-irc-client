package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public abstract class MessageCommand<T> extends IrcCommand {
    private T target;

    protected MessageCommand(@NonNull CommandBuilder builder, @NonNull T target) {
        super(builder);
        assert builder.getTrailingParameter() != null;
        setTarget(target);
    }

    @NonNull
    public String getMessage() {
        return getTrailingParameter().get();
    }

    @NonNull
    public T getTarget() {
        return target;
    }

    protected void setTarget(@NonNull T target) {
        this.target = target;
    }
}
