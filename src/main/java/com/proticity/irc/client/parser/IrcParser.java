package com.proticity.irc.client.parser;

import com.proticity.irc.client.command.IrcCommand;
import reactor.core.publisher.Flux;

/**
 * A stateless parser for IRC messages from the server to the client.
 *
 * This parser is stateless and can operate on multiple messages in parallel.
 *
 * @see IrcCommand
 * @see IrcInput
 */
public class IrcParser {
    /**
     * Constructs a new {@link IrcParser}.
     */
    public IrcParser() {
    }

    /**
     * Transforms a reactive stream of incoming IRC messages into parsed forms.
     *
     * @param inputs A {@link Flux} of incoming IRC messages from the server.
     *
     * @return A {@link Flux} of {@link IrcCommand} objects representing parsed messages.
     */
    public Flux<IrcCommand> messages(Flux<String> inputs) {
        return inputs.map(IrcInput::new).flatMap(IrcInput::messages);
    }

    /**
     * Transforms an incoming IRC messages into parsed forms.
     *
     * @param input An incoming IRC message from the server.
     *
     * @return A {@link Flux} of {@link IrcCommand} objects representing parsed messages.
     */
    public Flux<IrcCommand> messages(String input) {
        return new IrcInput(input).messages();
    }
}
