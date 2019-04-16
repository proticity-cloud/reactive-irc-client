package com.proticity.irc.client.parser;

import com.proticity.irc.client.command.Channel;
import com.proticity.irc.client.command.PrivmsgCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

public class IrcParserTest {
    @Test
    @SuppressWarnings("unchecked")
    public void testSingleCommand() {
        var parser = new IrcParser();
        var cmds = parser.messages("@tagName=value :nick!user@server.com PRIVMSG #foo :Hello, World!\r\n");
        cmds.doOnNext(cmd -> {
            Assertions.assertEquals(new Channel("#foo"), ((PrivmsgCommand<Channel>) cmd).getTarget());
            Assertions.assertEquals("Hello, World!", ((PrivmsgCommand<Channel>) cmd).getMessage());
        }).blockLast();
    }

    @Test
    public void testCommandFlux() {
        var parser = new IrcParser();
        var cmds = parser.messages(Flux.just("@tagName=value :nick!user@server.com PRIVMSG #foo :Hello, World!\r\n:nick!user@server.com PRIVMSG Frank :Hi",
                ":nick!user@server.com JOIN #chan"));
        var cmdList = cmds.collect(Collectors.toList()).block();

        Assertions.assertEquals(3, cmdList.size());
    }
}
