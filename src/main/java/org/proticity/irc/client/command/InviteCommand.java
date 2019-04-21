package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class InviteCommand extends IrcCommand {
    private Channel channel;
    private User user;

    public InviteCommand(@NonNull CommandBuilder builder) {
        super(builder);
        setChannel(channelFromParameter(1));
        setUser(new User(getParameters().get(0)));
    }

    @NonNull
    public Channel getChannel() {
        return channel;
    }

    protected void setChannel(@NonNull Channel channel) {
        this.channel = channel;
    }

    @NonNull
    public User getUser() {
        return user;
    }

    protected void setUser(@NonNull User user) {
        this.user = user;
    }
}
