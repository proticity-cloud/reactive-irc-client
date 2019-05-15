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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

public class IrcCommand {
    private static final Pattern CHANNEL = Pattern.compile("^(?<prefix>[#+&]|(![A-Z0-9]{5}))(?<name>[^ \0\r\n:,\u0007]+)");

    private Map<TagKey, String> tags;
    private Prefix prefix;
    private String command;
    private List<String> parameters;
    private String trailingParameter;

    protected IrcCommand() {
    }

    public IrcCommand(@NonNull CommandBuilder builder) {
        setTags(builder.getTags());
        setPrefix(builder.getPrefix());
        setCommand(builder.getCommand());
        setParameters(builder.getParameters());
        setTrailingParameter(builder.getTrailingParameter());
    }

    @NonNull
    @Override
    public String toString() {
        var builder = new StringBuilder();

        if (tags != null) {
            builder.append('@');
            for (var entry : tags.entrySet()) {
                builder.append(entry.getKey().toString());
                if (entry.getValue() != null) {
                    builder.append('=').append(entry.getValue());
                }
            }
            builder.append(' ');
        }

        if (prefix != null) {
            builder.append(prefix.toString()).append(' ');
        }

        builder.append(getCommand()).append(' ');
        if (parameters != null && parameters.size() > 0) {
            for (var param : parameters) {
                builder.append(param).append(' ');
            }
        }
        if (trailingParameter != null) {
            builder.append(':').append(trailingParameter);
        } else {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    protected Channel channelFromParameter(int index) {
        if (index >= getParameters().size()) {
            // TODO: Better error
            throw new RuntimeException();
        }
        var matcher = CHANNEL.matcher(getParameters().get(index));
        if (!matcher.lookingAt()) {
            // TODO: Better error
            throw new RuntimeException();
        }
        return new Channel(matcher.group("prefix"), matcher.group("name"));
    }

    @NonNull
    public Map<TagKey, String> getTags() {
        return Objects.requireNonNullElse(tags, Collections.emptyMap());
    }

    protected void setTags(@Nullable Map<TagKey, String> tags) {
        this.tags = tags;
    }

    @NonNull
    public Optional<Prefix> getPrefix() {
        return Optional.ofNullable(prefix);
    }

    protected void setPrefix(@Nullable Prefix prefix) {
        this.prefix = prefix;
    }

    @NonNull
    public String getCommand() {
        return command;
    }

    protected void setCommand(@NonNull String command) {
        this.command = command;
    }

    @NonNull
    public List<String> getParameters() {
        return Objects.requireNonNullElse(parameters, Collections.emptyList());
    }

    protected void setParameters(@Nullable List<String> parameters) {
        this.parameters = parameters;
    }

    @NonNull
    public Optional<String> getTrailingParameter() {
        return Optional.ofNullable(trailingParameter);
    }

    protected void setTrailingParameter(@Nullable String trailingParameter) {
        this.trailingParameter = trailingParameter;
    }
}
