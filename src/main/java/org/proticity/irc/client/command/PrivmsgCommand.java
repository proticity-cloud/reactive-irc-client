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

import java.util.regex.Pattern;

import reactor.util.annotation.NonNull;

public class PrivmsgCommand<T> extends MessageCommand<T> {
    private static final Pattern ACTION = Pattern.compile("^\u0001ACTION (?<action>[^\u0001]*)\u0001$");

    private String action;

    public PrivmsgCommand(@NonNull CommandBuilder builder, @NonNull T target) {
        super(builder, target);
        var matcher = ACTION.matcher(super.getMessage());
        if (matcher.lookingAt()) {
            action = matcher.group("action");
        }
    }

    public boolean isAction() {
        return action != null;
    }

    @NonNull
    @Override
    public String getMessage() {
        return isAction() ? action : super.getMessage();
    }
}
