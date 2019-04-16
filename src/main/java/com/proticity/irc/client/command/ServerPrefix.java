package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

public class ServerPrefix extends Prefix {
    private String serverName;

    public ServerPrefix(@NonNull String serverName) {
        setServerName(serverName);
    }

    @NonNull
    @Override
    public String toString() {
        return ":" + getServerName();
    }

    @NonNull
    public String getServerName() {
        return serverName;
    }

    public void setServerName(@NonNull String serverName) {
        this.serverName = serverName;
    }
}
