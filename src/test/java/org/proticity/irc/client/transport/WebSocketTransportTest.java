package org.proticity.irc.client.transport;

import org.junit.jupiter.api.AfterEach;
import reactor.netty.DisposableServer;

public class WebSocketTransportTest {
    private DisposableServer httpServer;

    @AfterEach
    public void disposeHttpServer() {
        if (httpServer != null)
            httpServer.disposeNow();
    }
}
