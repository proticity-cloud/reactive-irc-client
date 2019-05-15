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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

public class KickCommand extends IrcCommand {
    private static final Pattern COMMA = Pattern.compile(",");

    private List<Channel> channels;
    private List<User> users;

    public KickCommand(@NonNull CommandBuilder builder) {
        super(builder);
        var chanList = COMMA.split(getParameters().get(0));
        channels = new ArrayList<>(chanList.length);
        for (var chan : chanList) {
            channels.add(new Channel(chan));
        }

        var userList = COMMA.split(getParameters().get(1));
        for (var user : userList) {
            users.add(new User(user));
        }
    }

    @NonNull
    public List<Channel> getChannels() {
        return Objects.requireNonNullElse(channels, Collections.emptyList());
    }

    protected void setChannels(@Nullable List<Channel> channels) {
        this.channels = channels;
    }

    @NonNull
    public List<User> getUsers() {
        return Objects.requireNonNullElse(users, Collections.emptyList());
    }

    protected void setUsers(@Nullable List<User> users) {
        this.users = users;
    }

    @NonNull
    public Optional<String> getMessage() {
        return getTrailingParameter();
    }
}
