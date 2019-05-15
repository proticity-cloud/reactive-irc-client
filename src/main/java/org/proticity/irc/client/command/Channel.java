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

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import reactor.util.annotation.NonNull;

public class Channel implements Serializable {
    private static final long serialVersionUID = 0L;
    private static final Pattern CHANNEL = Pattern.compile("^(?<prefix>[#+&]|(![A-Z0-9]{5}))(?<name>[^ \0\r\n:,\u0007]+)$");

    private String prefix;
    private String name;

    public Channel(@NonNull String channel) {
        var matcher = CHANNEL.matcher(channel);
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException("Channel expression '" + channel + "' is not valid.");
        }
        setPrefix(matcher.group("prefix"));
        setName(matcher.group("name"));
    }

    public Channel(@NonNull String prefix, @NonNull String name) {
        setPrefix(prefix);
        setName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Channel channel = (Channel) o;
        return prefix.equals(channel.prefix) &&
                name.equals(channel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, name);
    }

    @NonNull
    @Override
    public String toString() {
        return getPrefix() + getName();
    }

    @NonNull
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(@NonNull String prefix) {
        this.prefix = prefix;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
