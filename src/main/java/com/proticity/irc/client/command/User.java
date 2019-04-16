package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class User implements Serializable {
    private String nickname;
    private String user;
    private String host;

    public User(@NonNull String nickname) {
        this(nickname, null, null);
    }

    public User(@NonNull String nickname, @Nullable String host) {
        this(nickname, null, host);
    }

    public User(@NonNull String nickname, @Nullable String user, @Nullable String host) {
        setNickname(nickname);
        setUser(user);
        setHost(host);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return nickname.equals(user.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickname);
    }

    @NonNull
    @Override
    public String toString() {
        return getNickname();
    }

    @NonNull
    public String getNickname() {
        return nickname;
    }

    protected void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
    }

    @NonNull
    public Optional<String> getUser() {
        return Optional.ofNullable(user);
    }

    protected void setUser(@Nullable String user) {
        this.user = user;
    }

    @NonNull
    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    protected void setHost(@Nullable String host) {
        this.host = host;
    }
}
