package com.proticity.irc.client.parser;

import com.proticity.irc.client.command.Channel;
import com.proticity.irc.client.command.CommandBuilder;
import com.proticity.irc.client.command.ErrorCommand;
import com.proticity.irc.client.command.InviteCommand;
import com.proticity.irc.client.command.IrcCommand;
import com.proticity.irc.client.command.JoinCommand;
import com.proticity.irc.client.command.NickCommand;
import com.proticity.irc.client.command.NicknamePrefix;
import com.proticity.irc.client.command.NoticeCommand;
import com.proticity.irc.client.command.NumericReplyCommand;
import com.proticity.irc.client.command.PartCommand;
import com.proticity.irc.client.command.PingCommand;
import com.proticity.irc.client.command.PongCommand;
import com.proticity.irc.client.command.PrivmsgCommand;
import com.proticity.irc.client.command.SQueryCommand;
import com.proticity.irc.client.command.ServerPrefix;
import com.proticity.irc.client.command.TagKey;
import com.proticity.irc.client.command.TopicCommand;
import com.proticity.irc.client.command.User;
import com.proticity.irc.client.command.twitch.WhisperCommand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IrcInputTest {
    @Test
    public void testTagBasicTag() {
        var builder = new CommandBuilder();
        new IrcInput("key=value ").tags(builder);
        Assertions.assertEquals(1, builder.getTags().size());
        Assertions.assertEquals("value", builder.getTags().get(new TagKey("key")));
    }

    @Test
    public void testTagMultiple() {
        var builder = new CommandBuilder();
        new IrcInput("key=value;foo=bar ").tags(builder);
        Assertions.assertEquals(2, builder.getTags().size());
        Assertions.assertEquals("value", builder.getTags().get(new TagKey("key")));
        Assertions.assertEquals("bar", builder.getTags().get(new TagKey("foo")));
    }

    @Test
    public void testTagNull() {
        var builder = new CommandBuilder();
        new IrcInput("key;foo=bar ").tags(builder);
        Assertions.assertEquals(2, builder.getTags().size());
        Assertions.assertNull(builder.getTags().get(new TagKey("key")));
        Assertions.assertEquals("bar", builder.getTags().get(new TagKey("foo")));
    }

    @Test
    public void testTagVendor() {
        var builder = new CommandBuilder();
        new IrcInput("twitch.tv/msg-id=1234 ").tags(builder);
        Assertions.assertEquals(1, builder.getTags().size());
        Assertions.assertEquals("1234", builder.getTags().get(new TagKey(false, "twitch.tv", "msg-id")));
    }

    @Test
    public void testTagEscapes() {
        var builder = new CommandBuilder();
        new IrcInput("key=\\;\\\\\\ \\\r\\\ntest ").tags(builder);
        Assertions.assertEquals(1, builder.getTags().size());
        Assertions.assertEquals("\\;\\\\\\ \\\r\\\ntest", builder.getTags().get(new TagKey("key")));
    }

    @Test
    public void testTagClientOnly() {
        var builder = new CommandBuilder();
        new IrcInput("+key=value ").tags(builder);
        Assertions.assertEquals(1, builder.getTags().size());
        for (var key : builder.getTags().keySet()) {
            Assertions.assertEquals(new TagKey(true, null, "key"), key);
        }
    }

    @Test
    public void testPrefixServerName() {
        var builder = new CommandBuilder();
        new IrcInput("testserver ").prefix(builder);
        Assertions.assertTrue(builder.getPrefix() instanceof ServerPrefix);
        Assertions.assertEquals("testserver", ((ServerPrefix) builder.getPrefix()).getServerName());
    }

    @Test
    public void testPrefixServerHostname() {
        var builder = new CommandBuilder();
        new IrcInput("test.server.com ").prefix(builder);
        Assertions.assertTrue(builder.getPrefix() instanceof ServerPrefix);
        Assertions.assertEquals("test.server.com", ((ServerPrefix) builder.getPrefix()).getServerName());
    }

    @Test
    public void testExtendedPrefixServerHostname() {
        var builder = new CommandBuilder();
        new IrcInput("test_.server.com ").prefix(builder);
        Assertions.assertTrue(builder.getPrefix() instanceof ServerPrefix);
        Assertions.assertEquals("test_.server.com", ((ServerPrefix) builder.getPrefix()).getServerName());
    }

    @Test
    public void testPrefixNickname() {
        var builder = new CommandBuilder();
        new IrcInput("bobby@server.com ").prefix(builder);
        Assertions.assertTrue(builder.getPrefix() instanceof NicknamePrefix);
        Assertions.assertEquals("bobby", ((NicknamePrefix) builder.getPrefix()).getNickname());
        Assertions.assertEquals("server.com", ((NicknamePrefix) builder.getPrefix()).getHost().get());
    }

    @Test
    public void testExtendedPrefixNickname() {
        var builder = new CommandBuilder();
        new IrcInput("3bobby@server.com ").prefix(builder);
        Assertions.assertTrue(builder.getPrefix() instanceof NicknamePrefix);
        Assertions.assertEquals("3bobby", ((NicknamePrefix) builder.getPrefix()).getNickname());
        Assertions.assertEquals("server.com", ((NicknamePrefix) builder.getPrefix()).getHost().get());
    }

    @Test
    public void testPrefixDefiniteNickAndUser() {
        var builder = new CommandBuilder();
        new IrcInput("bob_ted!user@myhost ").prefix(builder);
        Assertions.assertTrue(builder.getPrefix() instanceof NicknamePrefix);
        Assertions.assertEquals("bob_ted", ((NicknamePrefix) builder.getPrefix()).getNickname());
        Assertions.assertEquals("user", ((NicknamePrefix) builder.getPrefix()).getUser().get());
        Assertions.assertEquals("myhost", ((NicknamePrefix) builder.getPrefix()).getHost().get());
    }

    @Test
    public void testPrefixParseError() {
        var builder = new CommandBuilder();
        try {
            new IrcInput("server.com!bob").prefix(builder);
            Assertions.fail("Parse error was not thrown.");
        } catch (IrcParseException e) {
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPrivmsgChannel() {
        var cmd = new IrcInput("@tagName=value :bob!user@server.com PRIVMSG #foo :Hello, World!\r\n").message();
        Assertions.assertTrue(cmd instanceof PrivmsgCommand);
        var chanMsg = (PrivmsgCommand<Channel>) cmd;
        Assertions.assertEquals(new Channel("#foo"), chanMsg.getTarget());
        Assertions.assertEquals("Hello, World!", chanMsg.getMessage());
    }

    @Test
    public void testWhisper() {
        var cmd = new IrcInput("@tagName=value :bob!user@server.com WHISPER Frank :Hello, World!\r\n").message();
        Assertions.assertTrue(cmd instanceof WhisperCommand);
        var whisper = (WhisperCommand) cmd;
        Assertions.assertEquals(new User("Frank"), whisper.getTarget());
        Assertions.assertEquals("Hello, World!", whisper.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoticeUser() {
        var cmd = new IrcInput("@tagName=value :bob!user@server.com NOTICE Fred :Hello, World!\r\n").message();
        Assertions.assertTrue(cmd instanceof NoticeCommand);
        var userMsg = (NoticeCommand<User>) cmd;
        Assertions.assertEquals(new User("Fred"), userMsg.getTarget());
        Assertions.assertEquals("Hello, World!", userMsg.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNoticeChannel() {
        var cmd = new IrcInput("@tagName=value :bob!user@server.com NOTICE #foo :Hello, World!\r\n").message();
        Assertions.assertTrue(cmd instanceof NoticeCommand);
        var userMsg = (NoticeCommand<Channel>) cmd;
        Assertions.assertEquals(new Channel("#foo"), userMsg.getTarget());
        Assertions.assertEquals("Hello, World!", userMsg.getMessage());
    }

    @Test
    public void testSquery() {
        var cmd = new IrcInput("@tagName=value :bob!user@server.com SQUERY Fred :Hello, World!\r\n").message();
        Assertions.assertTrue(cmd instanceof SQueryCommand);
        var userMsg = (SQueryCommand) cmd;
        Assertions.assertEquals(new User("Fred"), userMsg.getTarget());
        Assertions.assertEquals("Hello, World!", userMsg.getMessage());
    }

    @Test
    public void testNumeric() {
        var cmd = new IrcInput(":bob!user@server.com 001 :Welcome, Bob\r\n").message();
        Assertions.assertTrue(cmd instanceof NumericReplyCommand);
        var numCmd = (NumericReplyCommand) cmd;
        Assertions.assertEquals(1, numCmd.getReplyCode());
        Assertions.assertEquals("Welcome, Bob", numCmd.getTrailingParameter().get());
    }

    @Test
    public void testGenericCommand() {
        var cmd = new IrcInput(":bob!user@server.com UNKNOWN Foo Bar 123 * :Hello, World\r\n").message();
        Assertions.assertEquals(IrcCommand.class, cmd.getClass());
        Assertions.assertEquals("UNKNOWN", cmd.getCommand());
        Assertions.assertEquals("Hello, World", cmd.getTrailingParameter().get());
        Assertions.assertEquals(4, cmd.getParameters().size());
        Assertions.assertEquals("Foo", cmd.getParameters().get(0));
        Assertions.assertEquals("Bar", cmd.getParameters().get(1));
        Assertions.assertEquals("123", cmd.getParameters().get(2));
        Assertions.assertEquals("*", cmd.getParameters().get(3));
    }

    @Test
    public void testPing() {
        var cmd = new IrcInput("PING :test.server.com\r\n").message();
        Assertions.assertTrue(cmd instanceof PingCommand);
        Assertions.assertEquals("test.server.com", ((PingCommand) cmd).getHost());
    }

    @Test
    public void testPong() {
        var cmd = new IrcInput("PONG :test.server.com\r\n").message();
        Assertions.assertTrue(cmd instanceof PongCommand);
        Assertions.assertEquals("test.server.com", ((PongCommand) cmd).getHost());
    }

    @Test
    public void testError() {
        var cmd = new IrcInput("ERROR :Bad input\r\n").message();
        Assertions.assertTrue(cmd instanceof ErrorCommand);
        Assertions.assertEquals("Bad input", ((ErrorCommand) cmd).getMessage());
    }

    @Test
    public void testJoin() {
        var cmd = new IrcInput(":nick@server.com JOIN #foo\r\n").message();
        Assertions.assertTrue(cmd instanceof JoinCommand);
        Assertions.assertEquals(new Channel("#foo"), ((JoinCommand) cmd).getChannel());
    }

    @Test
    public void testPart() {
        var cmd = new IrcInput(":nick@server.com PART #foo\r\n").message();
        Assertions.assertTrue(cmd instanceof PartCommand);
        Assertions.assertEquals(new Channel("#foo"), ((PartCommand) cmd).getChannel());
    }

    @Test
    public void testNick() {
        var cmd = new IrcInput(":nick@server.com NICK Bob\r\n").message();
        Assertions.assertTrue(cmd instanceof NickCommand);
        Assertions.assertEquals("Bob", ((NickCommand) cmd).getNickname());
    }

    @Test
    public void testTopic() {
        var cmd = new IrcInput(":nick@server.com TOPIC #foo :Welcome to the channel\r\n").message();
        Assertions.assertTrue(cmd instanceof TopicCommand);
        Assertions.assertEquals(new Channel("#foo"), ((TopicCommand) cmd).getChannel());
        Assertions.assertEquals("Welcome to the channel", ((TopicCommand) cmd).getTopic());
    }

    @Test
    public void testInvite() {
        var cmd = new IrcInput(":nick@server.com INVITE Bob #foo\r\n").message();
        Assertions.assertTrue(cmd instanceof InviteCommand);
        Assertions.assertEquals(new Channel("#foo"), ((InviteCommand) cmd).getChannel());
        Assertions.assertEquals(new User("Bob"), ((InviteCommand) cmd).getUser());
    }
}
