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
package org.proticity.irc.client.transport;

import javax.annotation.ParametersAreNonnullByDefault;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;

/**
 * A {@link Transport} implementation which communicates IRC over Websocket.
 */
@ParametersAreNonnullByDefault
public class WebSocketTransport implements Transport {
    /**
     * A {@link Mono} for a connection that will be used for the webSocket transport.
     */
    private Mono<? extends Connection> connection;

    /**
     * The default port for insecure connections.
     */
    private static final int DEFAULT_INSECURE_PORT = 80;

    /**
     * The default port for secure connections.
     */
    private static final int DEFAULT_SECURE_PORT = 443;

    /**
     * Create a new {@link WebSocketTransport} from a Netty Reactor {@link Connection}.
     *
     * @param connection A {@link Connection} which must be a webSocket {@link HttpClient}
     *                   connection.
     */
    public WebSocketTransport(Mono<? extends Connection> connection) {
        this.connection = connection.cache();
    }

    public void close() {
        dispose().block();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> dispose() {
        return connection.doOnNext(Connection::dispose).then();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<String> receive() {
        return connection.map(conn -> (WebsocketInbound) conn.inbound())
                .flatMapMany(WebsocketInbound::receiveFrames)
                .filter(frame -> frame instanceof TextWebSocketFrame)
                .map(frame -> ((TextWebSocketFrame) frame).text());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> send(Publisher<String> publisher) {
        return connection.flatMap(conn -> conn.outbound().sendString(publisher).then());
    }

    public static WebSocketTransport createSecure(String uri) {
        return createSecure(uri, DEFAULT_SECURE_PORT);
    }

    public static WebSocketTransport createSecure(String uri, int port) {
        return new WebSocketTransport(HttpClient.create().secure().port(port).websocket()
                .uri(uri).connect());
    }

    public static WebSocketTransport createInsecure(String uri) {
        return createInsecure(uri, DEFAULT_INSECURE_PORT);
    }

    public static WebSocketTransport createInsecure(String uri, int port) {
        return new WebSocketTransport(HttpClient.create().secure().port(port).websocket()
                .uri(uri).connect());
    }
}
