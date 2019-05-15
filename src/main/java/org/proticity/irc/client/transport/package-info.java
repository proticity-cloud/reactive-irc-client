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
/**
 * Transport layers which can communicate with an IRC server.
 *
 * IRC generally is communicated over a raw TCP connection or one tunneled through TLS. However
 * Twitch supports IRC over Websockets as well. The
 * {@link org.proticity.irc.client.transport.Transport} mechanism allows the protocol parsing and
 * responses to be separated from the means by which it is communicated so it can work with these
 * extensions.
 */
package org.proticity.irc.client.transport;
