 module com.proticity.irc.client {
    exports com.proticity.irc.client;
    exports com.proticity.irc.client.command;
    exports com.proticity.irc.client.transport;

    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires org.reactivestreams;
    requires reactor.core;
    requires reactor.netty;
}