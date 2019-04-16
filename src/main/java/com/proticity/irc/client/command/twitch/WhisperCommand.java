package com.proticity.irc.client.command.twitch;

import com.proticity.irc.client.command.CommandBuilder;
import com.proticity.irc.client.command.MessageCommand;
import com.proticity.irc.client.command.User;
import com.proticity.irc.client.parser.IrcParseException;
import reactor.util.annotation.NonNull;

import java.util.Objects;

public class WhisperCommand extends MessageCommand<User> implements TwitchCommand {
    public WhisperCommand(@NonNull CommandBuilder builder) {
        super(builder, new User(builder.getParameter(0)));
    }
}
