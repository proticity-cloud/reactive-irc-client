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
