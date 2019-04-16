package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public class Channel implements Serializable {
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
