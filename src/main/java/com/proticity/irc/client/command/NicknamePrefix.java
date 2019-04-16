package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.util.Optional;

public class NicknamePrefix extends Prefix {
    private String nickname;
    private String user;
    private String host;

    public NicknamePrefix(@NonNull String nickname, @Nullable String user, @Nullable String host) {
        setNickname(nickname);
        setUser(user);
        setHost(host);
    }

    @NonNull
    @Override
    public String toString() {
        return ":" + nickname + getUser().map(user -> "!" + user).orElse("") +
                getHost().map(host -> "@" + host).orElse("");
    }

    @NonNull
    public String getNickname() {
        return nickname;
    }

    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
    }

    @NonNull
    public Optional<String> getUser() {
        return Optional.ofNullable(user);
    }

    public void setUser(@Nullable String user) {
        this.user = user;
    }

    @NonNull
    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public void setHost(@Nullable String host) {
        this.host = host;
    }
}
