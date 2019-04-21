package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandBuilder {
    private Map<TagKey, String> tags;
    private Prefix prefix;
    private String command;
    private List<String> parameters;
    private String trailingParameter;

    public CommandBuilder() {
    }

    public CommandBuilder tag(@NonNull TagKey key, @Nullable String value) {
        if (tags == null) {
            tags = new HashMap<>();
        }
        tags.put(key, value);
        return this;
    }

    public CommandBuilder prefix(@Nullable Prefix prefix) {
        this.prefix = prefix;
        return this;
    }

    public CommandBuilder command(@NonNull String command) {
        this.command = command;
        return this;
    }

    public CommandBuilder parameter(String parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
        return this;
    }

    public CommandBuilder trailingParameter(@Nullable String trailingParameter) {
        this.trailingParameter = trailingParameter;
        return this;
    }

    @Nullable
    public Map<TagKey, String> getTags() {
        return tags;
    }

    @Nullable
    public Prefix getPrefix() {
        return prefix;
    }

    @NonNull
    public String getCommand() {
        return command;
    }

    @Nullable
    public List<String> getParameters() {
        return parameters;
    }

    @Nullable
    public String getTrailingParameter() {
        return trailingParameter;
    }

    @NonNull
    public String getParameter(int index) {
        if (parameters == null || parameters.size() <= index) {
            throw new IllegalArgumentException("Expected parameter " + index + " not found.");
        }
        String param = parameters.get(index);
        if (param == null) {
            throw new IllegalArgumentException("Expected parameter " + index + " not found.");
        }
        return param;
    }
}
