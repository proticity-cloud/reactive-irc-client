package org.proticity.irc.client.command;

import reactor.util.annotation.NonNull;

import java.io.Serializable;

public abstract class Prefix implements Serializable {
    @NonNull
    @Override
    public abstract String toString();
}
