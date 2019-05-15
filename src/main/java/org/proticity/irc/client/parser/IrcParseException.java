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
package org.proticity.irc.client.parser;

import reactor.util.annotation.NonNull;

public class IrcParseException extends RuntimeException {
    private String input;
    private int position;

    public IrcParseException(@NonNull String input, int position) {
        this(input, position, (Throwable) null);
    }

    public IrcParseException(@NonNull String input, int position, String message) {
        this(input, position, message, null);
    }

    public IrcParseException(@NonNull String input, int position, Throwable cause) {
        this(input, position, "Parse error at position " + position + ", in line '" +
                input.replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\\", "\\\\")
                .replace("\t", "\\t") + "'.", cause);
    }

    public IrcParseException(@NonNull String input, int position, String message, Throwable cause) {
        super(message, cause);
        this.input = input;
        this.position = position;
    }

    @NonNull
    public String getInput() {
        return input;
    }

    public int getPosition() {
        return position;
    }
}
