package com.proticity.irc.client.parser;

import com.proticity.irc.client.command.*;
import com.proticity.irc.client.command.twitch.WhisperCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcInput implements Serializable {
    // Hostname pattern extends the standard by allowing underscrores, required for Twitch which forms a nickname-based
    // hostname for hostname prefixes. Since nicknames can have underscores this means Twitch will form illegal hostnames.
    private static final String HOSTNAME_PATTERN = "(?<host>[a-zA-Z0-9][a-zA-Z0-9\\-_]*(\\.[a-zA-Z0-9][a-zA-Z0-9\\-_]*)*)";
    private static final String IPV4_PATTERN = "(?<ipv4>[0-9]?[0-9]?[0-9]\\.[0-9]?[0-9]?[0-9]\\.[0-9]?[0-9]?[0-9]\\.[0-9]?[0-9]?[0-9])";
    private static final String IPV6_PATTERN = "(?<ipv6>123abc)"; // TODO: Proper IPv6 support.
    // The nickname pattern extends the standard by allowing the first character to be numeric, required by Twitch.
    private static final String NICKNAME_PATTERN  = "(?<nick>[a-zA-Z\\[-`{-}0-9][a-zA-Z\\[-`{-}0-9\\-]*)";
    private static final String USER_PATTERN = "(?<user>[^ \0\r\n@]+)";

    private static final Pattern NICKNAME = Pattern.compile("^" + NICKNAME_PATTERN);
    private static final Pattern CHANNEL = Pattern.compile("^(?<prefix>[#+&]|(![A-Z0-9]{5}))(?<name>[^ \0\r\n:,\u0007]+)");
    private static final Pattern NUMERIC_REPLY = Pattern.compile("^[0-9]{3}");
    private static final Pattern NONCRLF = Pattern.compile("^[^\r\n]*");
    private static final Pattern PARAM = Pattern.compile("^[^ \r\n:]+");
    private static final Pattern TAG_KEY = Pattern.compile("^[a-zA-Z0-9\\-]+");
    private static final Pattern TAG_VENDOR = Pattern.compile("^" + HOSTNAME_PATTERN + "/");
    private static final Pattern TAG_VALUE = Pattern.compile("^(\\\\[ ;\r\n\0]|[^ ;\r\n\0])*");
    private static final Pattern SERVER_PREFIX = Pattern.compile("^" + HOSTNAME_PATTERN + "|" + IPV4_PATTERN + "|" + IPV6_PATTERN);
    private static final Pattern NICK_PREFIX = Pattern.compile("^" + NICKNAME_PATTERN + "((!" + USER_PATTERN + ")?@(" + HOSTNAME_PATTERN + "|" + IPV4_PATTERN + "|" + IPV6_PATTERN +"))?");
    private static final Pattern COMMAND = Pattern.compile("^[a-zA-Z0-9]+");

    private String input;
    private int position;

    public IrcInput(String input) {
        this.input = input;
    }

    public Flux<IrcCommand> messages() {
        tryCrlf();
        return Flux.create((FluxSink<IrcCommand> sink) -> {
            while (!tryEof()) {
                try {
                    sink.next(message());
                } catch (IrcParseException e) {
                    sink.next(new InvalidCommand(e.getInput(), e));
                }
                if (!tryCrlf()) {
                    break;
                }
            }
            sink.complete();
        });
    }

    protected IrcCommand message() {
        var builder = new CommandBuilder();
        if (tryConsume('@')) {
            tags(builder);
            space();
        }
        if (tryConsume(':')) {
            prefix(builder);
            space();
        }
        return command(builder);
    }

    protected IrcCommand command(@NonNull CommandBuilder builder) {
        builder.command(consume(COMMAND).group());
        while (trySpace()) {
            if (tryConsume(':')) {
                builder.trailingParameter(consume(NONCRLF).group());
                break;
            }
            builder.parameter(consume(PARAM).group());
        }

        switch (builder.getCommand()) {
            case "PRIVMSG":
                return privmsg(builder, PrivmsgCommand.class);
            case "ERROR":
                return new ErrorCommand(builder);
            case "NOTICE":
                return privmsg(builder, NoticeCommand.class);
            case "WHISPER":
                return privmsg(builder, WhisperCommand.class);
            case "PING":
                return new PingCommand(builder);
            case "PONG":
                return new PongCommand(builder);
            case "JOIN":
                return new JoinCommand(builder);
            case "PART":
                return new PartCommand(builder);
            case "NICK":
                return new NickCommand(builder);
            case "TOPIC":
                return new TopicCommand(builder);
            case "MODE":
                return new ModeCommand(builder);
            case "KICK":
                // TODO
            case "INVITE":
                return new InviteCommand(builder);
            case "SQUERY":
                return privmsg(builder, SQueryCommand.class);
            default:
                var matcher = NUMERIC_REPLY.matcher(builder.getCommand());
                if (matcher.lookingAt()) {
                    return new NumericReplyCommand(builder);
                }
                return new IrcCommand(builder);
        }
    }

    @Nullable
    protected MessageCommand<?> privmsg(@NonNull CommandBuilder builder, @NonNull Class<?> commandClass) {
        var params = builder.getParameters();
        if (params == null || params.isEmpty()) {
            // TODO: proper error
            parseError();
            return null;
        }
        var chanName = params.get(0);
        if (chanName == null) {
            parseError();
            return null;
        }
        if (commandClass.equals(SQueryCommand.class)) {
            return new SQueryCommand(builder);
        } else if (commandClass.equals(WhisperCommand.class)) {
            return new WhisperCommand(builder);
        } else {
            var chan = CHANNEL.matcher(chanName);
            if (chan.lookingAt()) {
                if (commandClass.equals(NoticeCommand.class)) {
                    return new NoticeCommand<>(builder,
                            new Channel(chan.group("prefix"), chan.group("name")));
                } else {
                    return new PrivmsgCommand<>(builder,
                            new Channel(chan.group("prefix"), chan.group("name")));
                }
            } else if (commandClass.equals(NoticeCommand.class)) {
                return new NoticeCommand<>(builder, new User(chanName));
            } else {
                return new PrivmsgCommand<>(builder, new User(chanName));
            }
        }
    }

    /**
     * Consume a capability.
     *
     *  Although not strictly defined as such, these use identical rules to tag keys, including the
     *  vendor, and so these rules are reused.
     *
     * @return A {@link Capability} representing the capability text.
     */
    protected Capability capability() {
        return new Capability(tryTagVendor().orElse(null), consume(TAG_KEY).group());
    }

    protected void tags(@NonNull CommandBuilder builder) {
        tag(builder);
        while (tryConsume(';')) {
            tag(builder);
        }
    }

    protected void tag(@NonNull CommandBuilder builder) {
        var key = tagKey();
        String value = null;
        if (tryConsume('=')) {
            value = tagValue();
        }
        builder.tag(key, value);
    }

    protected TagKey tagKey() {
        var clientOnly = tryConsume('+');
        var vendor = tryTagVendor();
        return new TagKey(clientOnly, vendor.orElse(null), consume(TAG_KEY).group());
    }

    /**
     * Attempt to parse a tag's vendor.
     *
     * A vendor portion of a name is permitted in a tag key, but is optional.
     *
     * @return The vendor in the tag name if one is present.
     */
    protected Optional<String> tryTagVendor() {
        return tryConsume(TAG_VENDOR).map(matcher -> matcher.group("host"));
    }

    protected String tagValue() {
        return consume(TAG_VALUE).group();
    }

    /**
     * Consume zero or more space characters, returning if any were found.
     *
     * @return Whether or not at least one space was consumed.
     */
    protected boolean trySpace() {
        var found = false;
        while (tryConsume(' ')) {
            found = true;
        }
        return found;
    }

    /**
     * Consume the required spaces in the input.
     *
     * This is permissive in that multiple spaces may be consumed as a single delimiter, however at
     * least one is required.
     */
    protected void space() {
        consumeAtLeastOne(' ');
    }

    /**
     * Consume zero or more CRLF sequences.
     *
     * This can be used where a CRLF may appear, such as at the very start of an input or at the
     * end.
     *
     * @return Whether at least one CRLF was present.
     */
    protected boolean tryCrlf() {
        boolean found = false;
        while (tryConsume("\r\n")) {
            found = true;
        }
        return found;
    }

    /**
     * Consume zero or one (or more, it's all the same) EOFs.
     *
     * This is used to peek for the end of the sequence.
     *
     * @return Whether or not the parser is at the end of the input.
     */
    protected boolean tryEof() {
        return position == input.length();
    }

    /**
     * Consume zero (or one, or more, it's all the same) EOFs.
     *
     * This will require the end of input and fail if the parser is elsewhere.
     */
    protected void eof() {
        if (position != input.length()) {
            parseError();
        }
    }

    /**
     * Parse the message prefix.
     *
     * This can be awkward because it requires backtracking. There is ambiguous grammar here and we
     * want to prioritize a servername prefix over a nickname prefix when both are valid.
     *
     * @param builder A {@link CommandBuilder}.
     */
    protected void prefix(@NonNull CommandBuilder builder) {
        int pos = position;
        var serverPrefix = tryConsume(SERVER_PREFIX);
        var hasServerPrefix = peek(' ');
        position = pos;
        var nickPrefix = tryConsume(NICK_PREFIX);
        var hasNickPrefix = peek(' ');
        if (!hasServerPrefix) {
            if (!hasNickPrefix) {
                parseError();
            }
            builder.prefix(new NicknamePrefix(nickPrefix.get().group("nick"),
                    nickPrefix.get().group("user"),
                    nickPrefix.get().group("host")));
        } else {
            position = pos + serverPrefix.get().group().length();
            builder.prefix(new ServerPrefix(serverPrefix.get().group()));
        }
    }

    protected boolean tryConsume(char c) {
        if (position == input.length()) {
            return false;
        }
        if (input.charAt(position) == c) {
            position++;
            return true;
        }
        return false;
    }

    /**
     * Peek at the next character without advancing the parser position.
     *
     * @param c The character to look for.
     *
     * @return Whether the next character of the input is <code>c</code>.
     */
    protected boolean peek(char c) {
        if (position == input.length()) {
            return false;
        }
        return input.charAt(position) == c;
    }

    protected boolean tryConsume(String s) {
        if (position + s.length() > input.length()) {
            return false;
        }
        if (input.substring(position, position + s.length()).equals(s)) {
            position += s.length();
            return true;
        }
        return false;
    }

    protected Optional<Matcher> tryConsume(Pattern pattern) {
        var matcher = pattern.matcher(input.substring(position));
        if (matcher.lookingAt()) {
            position += matcher.group().length();
            return Optional.of(matcher);
        }
        return Optional.empty();
    }

    protected Matcher consume(Pattern pattern) {
        return tryConsume(pattern).orElseThrow(this::createParseError);
    }

    protected void consumeAtLeastOne(char c) {
        boolean oneOrMore = false;
        while (tryConsume(c)) {
            oneOrMore = true;
        }
        if (!oneOrMore) {
            parseError();
        }
    }

    /**
     * Returns a parser exception.
     *
     * @return A new parser exception.
     */
    protected IrcParseException createParseError() {
        return new IrcParseException(input, position);
    }

    /**
     * Throws a parser exception.
     */
    protected void parseError() {
        throw createParseError();
    }
}
