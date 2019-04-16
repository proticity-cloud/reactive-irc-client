/**
 * Transport layers which can communicate with an IRC server.
 *
 * IRC generally is communicated over a raw TCP connection or one tunneled through TLS. However
 * Twitch supports IRC over Websockets as well. The
 * {@link com.proticity.irc.client.transport.Transport} mechanism allows the protocol parsing and
 * responses to be separated from the means by which it is communicated so it can work with these
 * extensions.
 */
package com.proticity.irc.client.transport;
