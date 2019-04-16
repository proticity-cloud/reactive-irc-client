package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class TopicCommand extends IrcCommand {
    private Channel channel;

    public TopicCommand(@NonNull CommandBuilder builder) {
        super(builder);
        setChannel(channelFromParameter(0));
    }

    @NonNull
    public String getTopic() {
        return getTrailingParameter().get();
    }

    @NonNull
    public Channel getChannel() {
        return channel;
    }

    protected void setChannel(@NonNull Channel channel) {
        this.channel = channel;
    }
}
