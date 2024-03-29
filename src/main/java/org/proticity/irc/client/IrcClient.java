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
package org.proticity.irc.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.proticity.irc.client.command.Capability;
import org.proticity.irc.client.command.InvalidCommand;
import org.proticity.irc.client.command.IrcCommand;
import org.proticity.irc.client.command.PingCommand;
import org.proticity.irc.client.parser.IrcParseException;
import org.proticity.irc.client.parser.IrcParser;
import org.proticity.irc.client.transport.TcpTransport;
import org.proticity.irc.client.transport.Transport;
import org.proticity.irc.client.transport.WebSocketTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides a client for publish/subscribe messaging using IRCv3.
 * <p>
 * The IrcClient works at the application layer and runs on top of a generic transport. As the
 * IrcClient is reactive it uses Netty Reactor for the underlying connection. When created, a
 * Netty Reactor Connection instance can be provided to be used for networking.
 * <p>
 * Twitch exposes TMI over TCP (as an extended version of IRCv3) and over WebSocket. Either
 * connection type will work. Convenience methods are available for creating IrcClients with default
 * connections. When using Twitch as a server Twitch support should also be specifically enabled to
 * negotiate for Twitch's extended capabilities.
 */
@ParametersAreNonnullByDefault
public class IrcClient {
    private static final IrcParser PARSER = new IrcParser();

    private static final char COLOR = (char) 27;

    private static final int MODE_WALLOPS = 4;

    private static final int MODE_INVISIBLE = 8;

    /**
     * A copy of the builder that was used to create the client.
     */
    private IrcClientBuilder builder;

    /**
     * A map of channels that have been joined to the channel information.
     */
    private Map<String, Boolean> channels = new ConcurrentHashMap<>();

    /**
     * The buffered writer for debug output.
     */
    private BufferedWriter bufferedDebugStreamWriter;

    /**
     * The output writer for the debug output.
     */
    private OutputStreamWriter debugStreamWriter;

    /**
     * The stream of inbound commands from the server.
     */
    private Flux<IrcCommand> inbound;

    protected IrcClient(final IrcClientBuilder builder) {
        this.builder = new IrcClientBuilder(builder);
        if (builder.debugStream != null) {
            debugStreamWriter = new OutputStreamWriter(builder.debugStream, StandardCharsets.UTF_8);
            bufferedDebugStreamWriter = new BufferedWriter(debugStreamWriter);
        }
        if (builder.transport == null) {
            throw new IllegalStateException("Transport has not been specified.");
        }
        inbound = builder.transport.receive().doOnNext(this::logInboundNext)
                .flatMap(PARSER::messages).doOnNext(this::handleMessage);
        // If error suppression is enabled, ignore IrcParseException.
        if (builder.suppressParseErrors) {
            inbound = inbound.onErrorContinue(err -> err instanceof IrcParseException, (err, input) -> { });
        }
        connect();
    }

    protected void connect() {
        int modes = 0;
        if (builder.receiveWallops) {
            modes |= MODE_WALLOPS;
        }
        if (builder.invisible) {
            modes |= MODE_INVISIBLE;
        }

        var capsMono = sendThen(Flux.fromIterable(builder.capabilities).map(cap -> "CAP REQ :" + cap.toString()));
        var passMono = sendThen(Flux.just("PASS " + builder.password));
        var userMono =
                sendThen(Flux.just("NICK " + builder.nickname,
                        "USER " + builder.user + " " + modes + " * :" + builder.realName));
        if (builder.password != null) {
            userMono = passMono.then(userMono);
        }
        capsMono.then(userMono).subscribe();
    }

    /**
     * Log a message from the server to the debug {@link OutputStream}.
     *
     * @param message The server message to log.
     */
    protected void logInboundNext(String message) {
        if (bufferedDebugStreamWriter == null) {
            return;
        }
        message = Instant.now().toString() + " < " + message;
        if (builder.colorizedDebug) {
            message = (char) COLOR + "[32m" + message + (char) COLOR + "[39m";
        }
        try {
            bufferedDebugStreamWriter.write(message, 0, message.length());
            bufferedDebugStreamWriter.newLine();
            bufferedDebugStreamWriter.flush();
        } catch (IOException e) {
            // TODO: Handle
        }
    }

    protected void logOutboundNext(String message) {
        if (bufferedDebugStreamWriter == null) {
            return;
        }
        message = Instant.now().toString() + " >> " + message;
        if (builder.colorizedDebug) {
            message = (char) COLOR + "[34m" + message + (char) COLOR + "[39m";
        }
        try {
            bufferedDebugStreamWriter.write(message, 0, message.length());
            bufferedDebugStreamWriter.newLine();
            bufferedDebugStreamWriter.flush();
        } catch (IOException e) {
            // TODO: Handle
        }
    }

    /**
     * Close the client.
     * <p>
     * This is a blocking call which will not return until the connection is closed. For a
     * non-blocking alternative use {@link IrcClient#dispose()}.
     */
    public void close() {
        dispose().block();
    }

    /**
     * Close the client without blocking.
     *
     * @return A {@link Mono} for the operation. A subscription to the value is required for
     * the disposal operation to be emitted.
     */
    public Mono<Void> dispose() {
        return builder.transport.dispose().doOnNext(v -> {
            if (bufferedDebugStreamWriter != null) {
                try {
                    bufferedDebugStreamWriter.close();
                } catch (IOException e) {
                    // TODO: Handle
                }
            }
            if (debugStreamWriter != null) {
                try {
                    debugStreamWriter.close();
                } catch (IOException e) {
                    // TODO: Handle
                }
            }
        });
    }

    /**
     * Returns a stream of commands coming from the server.
     *
     * @return The commands coming from the server.
     */
    public Flux<IrcCommand> commands() {
        return inbound;
    }

    /**
     * The built in command handler, where the client itself handles routine tasks.
     * <p>
     * This primarily handles PING commands to prevent disconnection.
     *
     * @param command The command received from the server.
     */
    protected void handleMessage(final IrcCommand command) {
        if (command instanceof PingCommand) {
            sendThen(Flux.just("PONG :" + ((PingCommand) command).getHost()));
        }
    }

    /**
     * Produces a publisher for the results of emitting commands to the server.
     * <p>
     * The {@link Mono} produced here represents the event of completion of the
     * sending of all commands. A subscription is required to actually emit
     * the sending of commands, i.e. none are sent until a subscription is made.
     *
     * @param commands The commands to issue.
     * @return A {@link Mono} for consuming send completion.
     */
    public Mono<Void> sendThen(final Flux<String> commands) {
        return builder.transport.send(commands.doOnNext(this::logOutboundNext)).then();
    }

    /**
     * Asynchronously sends commands to the server.
     * <p>
     * This is equivalent to calling {@link IrcClient#sendThen(Flux)} and then
     * subscribing, so it therefore will actually issue the commands.
     *
     * @param commands The commands to issue.
     * @return A {@link CompletableFuture} which can be used to determine when
     * the commands have been sent.
     */
    public CompletableFuture<Void> send(final Flux<String> commands) {
        return sendThen(commands).toFuture();
    }

    /**
     * Sends commands immediately and blocks until the commands are sent.
     *
     * @param commands The commands to send.
     */
    public void sendNow(final Flux<String> commands) {
        sendThen(commands).block();
    }

    /**
     * Sends commands immediately and blocks until the commands are sent or the
     * timeout period is exhausted.
     *
     * @param commands The commands to send.
     * @param timeout  The timeout period after which the method will stop blocking.
     */
    public void sendNow(final Flux<String> commands, final Duration timeout) {
        sendThen(commands).block(timeout);
    }

    /**
     * Normalizes a channel name for consistency.
     *
     * @param channel A denormalized channel name.
     * @return A normalized channel name (always preceded with a '#' character and in all lower
     * case).
     */
    private static String normalizeChannelName(String channel) {
        if (channel == null) {
            throw new IllegalArgumentException("Argument 'channel' cannot be null.");
        }
        if (!channel.startsWith("#")) {
            channel = "#" + channel;
        }
        return channel.toLowerCase(Locale.ROOT);
    }

    /**
     * Join a new channel.
     * <p>
     * This will join the given channel if it is not already joined. The return value indicates if
     * the join operation made a change or if the channel was already joined.
     *
     * @param channel The channel to join.
     * @return A {@link Mono} representing the completion of the join.
     */
    public Mono<Boolean> join(String channel) {
        var channelName = normalizeChannelName(channel);
        return sendThen(Flux.just("JOIN " + channelName)).map(t -> channels.putIfAbsent(channelName, true) != null);
    }

    public Mono<Boolean> part(String channel) {
        var channelName = normalizeChannelName(channel);
        return sendThen(Flux.just("PART " + channelName)).map(t -> channels.remove(channelName) != null);
    }

    /**
     * Prepare a new IRC client.
     *
     * @return A builder for a new IRC client.
     */
    public static IrcClientBuilder create() {
        return new IrcClientBuilder();
    }

    /**
     * A builder for new instances of the {@link IrcClient}.
     */
    @ParametersAreNonnullByDefault
    public static class IrcClientBuilder {
        /**
         * The debugging {@link OutputStream} to use, if any.
         */
        private OutputStream debugStream;

        /**
         * Whether debugging output should be colorized.
         */
        private boolean colorizedDebug;

        /**
         * The user the client will use for login.
         */
        private String user;

        /**
         * The OAuth token the client will use for login.
         */
        private String password;

        /**
         * The nickname of the user after connection.
         */
        private String nickname;

        /**
         * The real name of the user.
         */
        private String realName;

        /**
         * The custom {@link Transport} to use to connect, if any.
         */
        private Transport transport;

        /**
         * A set of capabilities to provide to the server.
         */
        private Set<Capability> capabilities;

        /**
         * Whether to suppress command parse errors on commands from the server.
         */
        private boolean suppressParseErrors;

        private boolean invisible;

        private boolean receiveWallops;

        /**
         * Creates a new {@link IrcClientBuilder}.
         */
        protected IrcClientBuilder() {
        }

        /**
         * Creates a new {@link IrcClientBuilder} which is a copy of an existing one.
         *
         * @param builder the existing builder to copy.
         */
        public IrcClientBuilder(final IrcClientBuilder builder) {
            receiveWallops = builder.receiveWallops;
            invisible = builder.invisible;
            nickname = builder.nickname;
            capabilities = new HashSet<>(builder.capabilities);
            colorizedDebug = builder.colorizedDebug;
            debugStream = builder.debugStream;
            transport = builder.transport;
            user = builder.user;
            password = builder.password;
            realName = builder.realName;
            suppressParseErrors = builder.suppressParseErrors;
        }

        /**
         * Enables debugging output for the client.
         * <p>
         * If not otherwise specified, sets the output to be on {@link System#out}.
         *
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder debug() {
            return debug(true);
        }

        /**
         * Control whether debug output for the client is enabled.
         *
         * @param enabled <code>true</code> to enable debug output, or <code>false</code> to disable
         *                it. If not already explicitly set enabling debugging will default to
         *                outputting to {@link System#out}.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder debug(boolean enabled) {
            return debug(enabled ? System.out : null);
        }

        /**
         * Control whether debug output for the client is enabled.
         *
         * @param enabled  <code>true</code> to enable debug output, or <code>false</code> to disable
         *                 it. If not already explicitly set enabling debugging will default to
         *                 outputting to {@link System#out}.
         * @param colorize Whether or not to colorize the debug output.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder debug(boolean enabled, boolean colorize) {
            colorizedDebug = colorize;
            return debug(enabled);
        }

        /**
         * Control whether debug output for the client is enabled.
         *
         * @param outputStream The output stream to use for debug output. If none is provided
         *                     debugging will be disabled.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder debug(@Nullable OutputStream outputStream) {
            debugStream = outputStream;
            return this;
        }

        /**
         * Control whether debug output for the client is enabled.
         *
         * @param outputStream The output stream to use for debug output. If none is provided
         *                     debugging will be disabled.
         * @param colorize     Whether or not to colorize the debug output.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder debug(@Nullable OutputStream outputStream,
                                      boolean colorize) {
            colorizedDebug = colorize;
            return debug(outputStream);
        }

        /**
         * Enables suppression of parse errors on commands from the server.
         * <p>
         * If enabled, this option will prevent errors from being emitted for parse exceptions from
         * {@link IrcClient#commands()}. Instead an {@link InvalidCommand} will be
         * automatically substituted for such errors in the command stream.
         *
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder suppressParseErrors() {
            return suppressParseErrors(true);
        }

        /**
         * Control whether to suppress parse errors on commands from the server.
         * <p>
         * If enabled, this option will prevent errors from being emitted for parse exceptions from
         * {@link IrcClient#commands()}. Instead an {@link InvalidCommand} will be
         * automatically substituted for such errors in the command stream.
         *
         * @param enabled Whether to enable parse error suppression.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder suppressParseErrors(boolean enabled) {
            this.suppressParseErrors = enabled;
            return this;
        }

        /**
         * Provide a user to be used by the bot.
         *
         * @param user The user to use.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder user(final String user) {
            this.user = user;
            return this;
        }

        /**
         * Provide a nickname for the client to use.
         *
         * @param nickname The client's nickname.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder nickname(final String nickname) {
            this.nickname = nickname;
            return this;
        }

        /**
         * Provide a real name for the client to use.
         *
         * @param realName The client's real name.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder realName(final String realName) {
            this.realName = realName;
            return this;
        }

        /**
         * Provide a password to be used by the bot for login.
         *
         * @param password The password to use for login.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Specifies a custom {@link Transport} to use to connect.
         *
         * @param transport The {@link Transport} to use to connect.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder transport(Transport transport) {
            this.transport = transport;
            return this;
        }

        /**
         * Use a default WebSocket/TLS transport to a given URI on default port 443.
         *
         * @param uri The URI on which to connect.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder webSocket(String uri) {
            transport = WebSocketTransport.createSecure(uri);
            return this;
        }

        /**
         * Use a default WebSocket/TLS transport to a given URI and port.
         *
         * @param uri  The URI to which to connect.
         * @param port The port on which to connect.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder webSocket(String uri, int port) {
            transport = WebSocketTransport.createSecure(uri, port);
            return this;
        }

        /**
         * Use a default TCP/TLS transport to a given host using default port 6697.
         *
         * @param host The host to which to connect.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder tcp(String host) {
            transport = TcpTransport.createSecure(host);
            return this;
        }

        /**
         * Use a default TCP/TLS transport to a given host and port.
         *
         * @param host The host to which to connect.
         * @param port The port on which to connect.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder tcp(String host, int port) {
            transport = TcpTransport.createSecure(host, port);
            return this;
        }

        /**
         * Enable Twitch IRC features (TMI).
         * <p>
         * Twitch uses the capabilities system to negotiate support for additional Twitch features.
         * If Twitch support is enabled these capabilities will be negotiated automatically and
         * Twitch features will be supported (examples are that Twitch will provide it's tags as
         * IRCv3 tags, and it will send WHISPER commands, which this client treats as a subclass
         * of user-to-user PRIVMSG commands).
         * <p>
         * If no transport has been set this will cause it to default to Twitch's TMI servers
         * using a secure WebSocket connection.
         *
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder twitch() {
            // If no transport is set, use WebSocket to connect to Twitch servers.
            if (transport == null) {
                transport = WebSocketTransport.createSecure("wss://irc-ws.chat.twitch.tv");
            }

            // Set default anonymous Twitch connection values.
            if (nickname == null) {
                nickname = "justinfan12345";
            }
            if (user == null) {
                user = "justinfan12345";
            }
            if (password == null) {
                password = "SCHMOOPIE";
            }

            // Add the Twitch capabilities.
            capability("twitch.tv/tags");
            capability("twitch.tv/membership");
            return capability("twitch.tv/commands");
        }

        /**
         * Add a capability to the resulting connection, which will be negotiated when the
         * connection is established.
         *
         * @param capability The capability to add.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder capability(final Capability capability) {
            if (capabilities == null) {
                capabilities = new HashSet<>();
            }
            capabilities.add(capability);
            return this;
        }

        /**
         * Add a capability to the resulting connection, which will be negotiated when the
         * connection is established.
         *
         * @param capability The capability to add.
         * @return The instance of the {@link IrcClientBuilder}.
         */
        public IrcClientBuilder capability(final String capability) {
            return capability(new Capability(capability));
        }

        public IrcClientBuilder invisible() {
            return invisible(true);
        }

        public IrcClientBuilder invisible(boolean enabled) {
            invisible = enabled;
            return this;
        }

        public IrcClientBuilder receiveWallops() {
            return receiveWallops(true);
        }

        public IrcClientBuilder receiveWallops(boolean enabled) {
            receiveWallops = enabled;
            return this;
        }

        /**
         * Produce the {@link IrcClient} for connection and use.
         * <p>
         * As the client is returned in a {@link Mono} the connection is not established until
         * the result is subscribed to.
         *
         * @return A {@link Mono} of an {@link IrcClient}.
         */
        public IrcClient connect() {
            if (transport == null) {
                throw new IllegalArgumentException("A transport must be provided.");
            }
            if (nickname == null) {
                throw new IllegalArgumentException("A nickname must be provided.");
            }
            if (user == null) {
                user = nickname;
            }
            return new IrcClient(this);
        }
    }
}
