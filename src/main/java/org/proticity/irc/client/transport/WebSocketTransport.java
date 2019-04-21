package org.proticity.irc.client.transport;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.websocket.WebsocketInbound;
import reactor.util.annotation.NonNull;

/**
 * A {@link Transport} implementation which communicates IRC over Websocket.
 */
public class WebSocketTransport implements Transport {
    /**
     * A {@link Mono} for a connection that will be used for the webSocket transport.
     */
    private Mono<? extends Connection> connection;

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

    @Override
    @NonNull
    public Mono<Void> dispose() {
        return connection.doOnNext(Connection::dispose).then();
    }

    @Override
    @NonNull
    public Flux<String> receive() {
        return connection.map(conn -> (WebsocketInbound) conn.inbound())
                .flatMapMany(WebsocketInbound::receiveFrames)
                .filter(frame -> frame instanceof TextWebSocketFrame)
                .map(frame -> ((TextWebSocketFrame) frame).text());
    }

    @Override
    @NonNull
    public Mono<Void> send(Publisher<String> publisher) {
        return connection.flatMap(conn -> conn.outbound().sendString(publisher).then());
    }

    @NonNull
    public static WebSocketTransport createSecure(String uri) {
        return createSecure(uri, 443);
    }

    @NonNull
    public static WebSocketTransport createSecure(String uri, int port) {
        return new WebSocketTransport(HttpClient.create().secure().port(port).websocket()
                .uri(uri).connect());
    }

    @NonNull
    public static WebSocketTransport createInsecure(String uri) {
        return createInsecure(uri, 80);
    }

    @NonNull
    public static WebSocketTransport createInsecure(String uri, int port) {
        return new WebSocketTransport(HttpClient.create().secure().port(port).websocket()
                .uri(uri).connect());
    }
}
