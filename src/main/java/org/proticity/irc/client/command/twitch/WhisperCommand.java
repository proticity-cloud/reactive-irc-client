package org.proticity.irc.client.command.twitch;

import org.proticity.irc.client.command.CommandBuilder;
import org.proticity.irc.client.command.MessageCommand;
import org.proticity.irc.client.command.User;
import reactor.util.annotation.NonNull;

public class WhisperCommand extends MessageCommand<User> implements TwitchCommand {
    public WhisperCommand(@NonNull CommandBuilder builder) {
        super(builder, new User(builder.getParameter(0)));
    }
}
