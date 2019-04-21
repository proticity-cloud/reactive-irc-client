package org.proticity.irc.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IrcClientTest {
    @Test
    public void testNoTransport() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                IrcClient.create().nickname("Nick").connect());
    }

    @Test
    public void testNoNickname() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                IrcClient.create().tcp("chat.freenode.net").connect());
    }
}
