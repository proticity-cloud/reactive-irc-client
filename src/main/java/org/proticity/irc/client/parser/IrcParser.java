/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2019 John Stewart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.proticity.irc.client.parser;

import org.proticity.irc.client.command.IrcCommand;
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
