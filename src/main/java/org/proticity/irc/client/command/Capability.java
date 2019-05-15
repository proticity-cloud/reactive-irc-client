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
import java.util.regex.Pattern;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

public class Capability implements Serializable {
    private static final long serialVersionUID = 0L;
    private static final Pattern CAPABILITY =
            Pattern.compile("^((?<vendor>[a-zA-Z0-9](-[a-zA-Z0-9]|[a-zA-Z0-9])*(\\.[a-zA-Z0-9](-[a-zA-Z0-9]|[a-zA-Z0-9])*)*)/)?(?<name>[a-zA-Z0-9\\-]+)$");

    private String vendor;
    private String name;

    public Capability(@NonNull String name) {
        var matcher = CAPABILITY.matcher(name);
        if (!matcher.lookingAt()) {
            throw new IllegalArgumentException("Name is not in the correct capability format.");
        }
        setVendor(matcher.group("vendor"));
        setName(matcher.group("name"));
    }

    public Capability(@Nullable String vendor, @NonNull String name) {
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
        Capability that = (Capability) o;
        return Objects.equals(vendor, that.vendor) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor, name);
    }

    @NonNull
    @Override
    public String toString() {
        return getVendor().map(v -> v + "/").orElse("") + getName();
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
