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

import java.io.Closeable;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
