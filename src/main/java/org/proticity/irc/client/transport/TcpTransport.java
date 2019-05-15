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

import java.nio.charset.Charset;

import javax.annotation.ParametersAreNonnullByDefault;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.tcp.TcpClient;

/**
 * A {@link Transport} implementation which communicates IRC over raw TCP, like IRC.
 */
@ParametersAreNonnullByDefault
public class TcpTransport implements Transport {
    /**
     * The default secure port for IRC with TLS port.
     */
    private static final int DEFAULT_SECURE_TCP_PORT = 6697;

    /**
     * The default insecure port for IRC.
     */
    private static final int DEFAULT_INSECURE_TCP_PORT = 6667;

    /**
     * A {@link Mono} for a connection that will be used for the TCP transport.
     */
    private Mono<? extends Connection> connection;

    public TcpTransport(Mono<? extends Connection> connection) {
        this.connection = connection.cache();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        return connection.map(Connection::inbound)
                .flatMapMany(NettyInbound::receive)
                .map(buf -> buf.toString(Charset.forName("UTF-8")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> send(Publisher<String> publisher) {
        return connection.flatMap(conn -> conn.outbound().sendString(publisher).then());
    }

    public static TcpTransport createSecure(String host) {
        return createSecure(host, DEFAULT_SECURE_TCP_PORT);
    }

    public static TcpTransport createSecure(String host, int port) {
        return new TcpTransport(TcpClient.create().secure().host(host)
                .port(port).connect());
    }

    public static TcpTransport createInsecure(String host) {
        return createInsecure(host, DEFAULT_INSECURE_TCP_PORT);
    }

    public static TcpTransport createInsecure(String host, int port) {
        return new TcpTransport(TcpClient.create().host(host)
                .port(port).connect());
    }
}
