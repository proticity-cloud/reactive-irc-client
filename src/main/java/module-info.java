 module org.proticity.irc.client {
    exports org.proticity.irc.client;
    exports org.proticity.irc.client.command;
    exports org.proticity.irc.client.transport;

    requires io.netty.buffer;
    requires io.netty.codec.http;
    requires org.reactivestreams;
    requires reactor.core;
    requires reactor.netty;
}