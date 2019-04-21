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
