package org.proticity.irc.client.transport;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * An interface for implementations of reactive network transport channels which can send and
 * receive a text protocol.
 */
public interface Transport extends Closeable {
    /**
     * Closes this transport and releases any system resources associated with it.
     *
     * This is a blocking call. To reactively close a transport see {@link Transport#dispose()}.
     *
     * @see Transport#dispose()
     */
    @Override
    void close();

    /**
     * Dispose of the transport reactively.
     *
     * This will emit the disposal of the underlying connection and reactor subscriptions. Since it
     * is a publisher a subscription to the return value is required to emit the disposal. For a
     * blocking disposal use {@link Transport#close()}.
     *
     * @see Transport#close()
     *
     * @return A {@link Mono} of the disposal of the transport.
     */
    Mono<Void> dispose();

    /**
     * Receive a stream of inbound text messages.
     *
     * @return A {@link Flux} which emits the incoming text messages from the server.
     */
    Flux<String> receive();

    /**
     * Sends messages to the server.
     *
     * @param publisher A publisher of the messages to send.
     *
     * @return A {@link Mono} emitting the completion of the send, can be used to react to the sends
     * completing.
     */
    Mono<Void> send(Publisher<String> publisher);
}
