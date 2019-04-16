package com.proticity.irc.client.command;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class TagKey implements Serializable {
    private boolean clientOnly;
    private String vendor;
    private String name;

    public TagKey(@NonNull String name) {
        this(false, null, name);
    }

    public TagKey(boolean clientOnly, @Nullable String vendor, @NonNull String name) {
        setClientOnly(clientOnly);
        setVendor(vendor);
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
        TagKey tagKey = (TagKey) o;
        return clientOnly == tagKey.clientOnly &&
                Objects.equals(vendor, tagKey.vendor) &&
                name.equals(tagKey.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientOnly, vendor, name);
    }

    @NonNull
    @Override
    public String toString() {

        return (isClientOnly() ? "+" : "") + getVendor().map(v -> v + "/").orElse("") + getName();
    }

    public boolean isClientOnly() {
        return clientOnly;
    }

    public void setClientOnly(boolean clientOnly) {
        this.clientOnly = clientOnly;
    }

    @NonNull
    public Optional<String> getVendor() {
        return Optional.ofNullable(vendor);
    }

    public void setVendor(@Nullable String vendor) {
        this.vendor = vendor;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }
}
