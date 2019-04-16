package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

// TODO: Better mode information
public class ModeCommand extends IrcCommand {
    private Channel channel;

    public ModeCommand(@NonNull CommandBuilder builder) {
        super(builder);
        setChannel(channelFromParameter(0));
    }

    @NonNull
    public Channel getChannel() {
        return channel;
    }

    protected void setChannel(@NonNull Channel channel) {
        this.channel = channel;
    }
}
