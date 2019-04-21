# Reactive IRC Client
This library is a reactive client for IRC and IRCv3/TMI (Twitch) extensions over TCP and WebSocket. It uses the Reactor
library for functional reactive streams to achieve very high throughput and non-blocking semantics.

## Features
* Standards-compliant support for the IRC RFC.
* Reduced strictness on some syntax (e.g. hostnames) to support extended servers such as Twitch's TMI.
* IRCv3 tagging and capabilities support.
* Functional reactive streams interface for high-performance non-blocking handling of I/O on top of Reactor Netty.
* Extensible support for IRC over various transports, with TCP and WebSocket available out of the box.
* Secure transport support (support for IRC over TLS).
* Strongly-typed event interfaces for incoming IRC commands.

## Adding the Library
Maven:
```xml
<dependencies>
    <dependency>
        <groupId>org.proticity</groupId>
        <artifactId>reactive-irc-client</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Gradle:
```groovy
dependencies {
    implementation 'com.proticity.irc:reactive-irc-client:1.0.0-SNAPSHOT'
}
```

## Using the Library
### Creating a Client
#### Standard IRC Server
A typical IRC server is available via unsecured TCP on port 6667. A `TcpTransport` will support default to port 6667.

```java
IrcClient client = IrcClient.create()
    .transport(TcpTransport.createInsecure("chat.freenode.net"))
    .nickname("BotUser")
    .realName("Bot User")
    .connect();
```

If the server supports TLS a secure transport can be created.

```java
IrcClient client = IrcClient.create()
    .transport(TcpTransport.createSecure("chat.freenode.net", 6697))
    .nickname("BotUser")
    .realName("Bot User")
    .connect();
```

The default port for a secure TCP transport is 6697 if none is given. For simplicity, `IrcClientBuilder#tcp()` can be
used to create a secure (and only a secure) `TcpTransport`.

```java
IrcClient client = IrcClient.create()
    .tcp("chat.freenode.net")
    .nickname("BotUser")
    .realName("Bot User")
    .connect();
```

#### WebSocket Clients
The `WebSocketTransport` supports connection over WebSocket. It's API is similar to that of `TcpTransport`.

```java
var insecureTransport = WebSocketTransport.createInsecure("ws://chat.server.com");
var secureTransport = WebSocketTransport.createInsecure("wss://chat.server.com");
```

These can be used with the builder's `IrcClientBuilder#transport()` method, or the convenience
`IrcClientBuilder#webSocket()` method can used.

```java
IrcClient client = IrcClient.create()
    .webSocket("wss://chat.server.com")
    .nickname("BotUser")
    .realName("Bot User")
    .connect();
```

#### Twitch Support
Twitch chat uses TMI, an extension to IRC with some IRCv3 features such as tagging and capabilities. These features are
supported as are the more liberal syntactic rules for some parts of the RFC's grammar. Twitch support merely requires
connecting to a Twitch TMI endpoint via the appropriate transport and optionally providing the capabilities to the
builder that you wish to use (`"twitch.tv/tags"`, `"twitch.tv/commands"`, `"twitch.tv/membership"`). A convenience
method is provided on the builder for Twitch connections to simplify the process. This method will add the necessary
capabilities, setup the transport for a secure WebSocket connection, and, if no nickname is set, will default it to
the anonymous `"justinfan12345"` nickname.

```java
// Connect as an anonymous Twitch user.
IrcClient client = IrcClient.create().twitch().connect();

client = IrcClient.create()
    .twitch()
    .nickname("MyBot")
    .password("oauth:abcdefg1234567")
    .suppressParseErrors()
    .connect();
```

The `suppressParseErrors` option is encouraged for Twitch as there are bugs in TMI where batched `JOIN` commands
are cut off after 2048 characters in normal use. This option will cause the client to simply ignore any command which
fails to parse (although it will be logged as a warning if you use an SLF4J logger).

### Receiving Commands
As a reactive library the IRC client does not connect until there is a subscriber. A `Flux` of commands from the server
will be received from the method `IrcClient#commands()`. The commands are strongly typed when possible. The base class
`IrcCommand` allows a command to be generically inspected, and typed subclasses include friendly methods for extracting
parts of the command parameters. Note that most numeric commands are generalized as a `NumericReplyCommand`.

```java
// Print all commands but call out PRIVMSG from a user specifically.
client.commands()
    .doOnNext(System.out::println) // Print every command to stdout
    .filter(cmd -> cmd instanceof PrivmsgCommand) // Then filter for just PRIVMSG
    .cast(PrivmsgCommand.class)
    .filter(cmd -> cmd.getPrefix().isPresent() && cmd.getPrefix().get() instanceof NicknamePrefix)
    .subscribe(cmd -> {
        System.out.println("You received a message from " + ((NicknamePrefix) cmd.getPrefix().get()).getNickname());
    });
```

#### Twitch Commands
When using Twitch with the appropriate command capability there are some extensions to IRC that Twitch has. This
library has strongly-typed Twitch command subclasses out of the box and will use them when received from a TMI server.
