package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class PartCommand extends IrcCommand {
    private Channel channel;

    public PartCommand(@NonNull CommandBuilder builder) {
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
