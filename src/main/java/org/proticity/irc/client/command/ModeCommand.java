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
package org.proticity.irc.client.command;

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
