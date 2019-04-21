package org.proticity.irc.client.transport;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.tcp.TcpClient;
import reactor.util.annotation.NonNull;

import java.nio.charset.Charset;

/**
 * A {@link Transport} implementation which communicates IRC over raw TCP, like IRC.
 */
public class TcpTransport implements Transport {
    /**
     * The default host for IRC.
     */
    private static final String DEFAULT_TCP_HOST = "irc.chat.twitch.tv";

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

    @Override
    public void close() {
        dispose().block();
    }

    @Override
    @NonNull
    public Mono<Void> dispose() {
        return connection.doOnNext(Connection::dispose).then();
    }

    @Override
    @NonNull
    public Flux<String> receive() {
        return connection.map(Connection::inbound)
                .flatMapMany(NettyInbound::receive)
                .map(buf -> buf.toString(Charset.forName("UTF-8")));
    }

    @Override
    @NonNull
    public Mono<Void> send(Publisher<String> publisher) {
        return connection.flatMap(conn -> conn.outbound().sendString(publisher).then());
    }

    @NonNull
    public static TcpTransport createSecure(String host) {
        return createSecure(host, 6697);
    }

    @NonNull
    public static TcpTransport createSecure(String host, int port) {
        return new TcpTransport(TcpClient.create().secure().host(host)
                .port(port).connect());
    }

    @NonNull
    public static TcpTransport createInsecure(String host) {
        return createInsecure(host, 6667);
    }

    @NonNull
    public static TcpTransport createInsecure(String host, int port) {
        return new TcpTransport(TcpClient.create().host(host)
                .port(port).connect());
    }
}
