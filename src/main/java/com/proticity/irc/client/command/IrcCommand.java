package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

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
