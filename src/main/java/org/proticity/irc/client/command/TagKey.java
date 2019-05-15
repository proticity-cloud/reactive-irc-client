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
import java.util.Optional;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

public class TagKey implements Serializable {
    private static final long serialVersionUID = 0L;

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
