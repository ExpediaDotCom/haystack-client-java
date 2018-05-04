/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.haystack.sleuth.core.haystack;

public interface SpanMarkers {
    /**
     * The client sent ("cs") a request to a server. There is only one send per span. For example, if
     * there's a transport error, each attempt can be logged as a [.WIRE_SEND] annotation.
     *
     *
     * If chunking is involved, each chunk could be logged as a separate [ ][.CLIENT_SEND_FRAGMENT] in the same span.
     *
     *
     * [Annotation.endpoint] is not the server. It is the host which logged the send event,
     * almost always the client. When logging CLIENT_SEND, instrumentation should also logger the [ ][.SERVER_ADDR].
     */
    String CLIENT_SEND = "cs";

    /**
     * The client received ("cr") a response from a server. There is only one receive per span. For
     * example, if duplicate responses were received, each can be logged as a [.WIRE_RECV]
     * annotation.
     *
     *
     * If chunking is involved, each chunk could be logged as a separate [ ][.CLIENT_RECV_FRAGMENT] in the same span.
     *
     *
     * [Annotation.endpoint] is not the server. It is the host which logged the receive
     * event, almost always the client. The actual endpoint of the server is recorded separately as
     * [.SERVER_ADDR] when [.CLIENT_SEND] is logged.
     */
    String CLIENT_RECV = "cr";

    /**
     * The server sent ("ss") a response to a client. There is only one response per span. If there's
     * a transport error, each attempt can be logged as a [.WIRE_SEND] annotation.
     *
     *
     * Typically, a trace ends with a server send, so the last timestamp of a trace is often the
     * timestamp of the root span's server send.
     *
     *
     * If chunking is involved, each chunk could be logged as a separate [ ][.SERVER_SEND_FRAGMENT] in the same span.
     *
     *
     * [Annotation.endpoint] is not the client. It is the host which logged the send event,
     * almost always the server. The actual endpoint of the client is recorded separately as [ ][.CLIENT_ADDR] when [.SERVER_RECV] is logged.
     */
    String SERVER_SEND = "ss";

    /**
     * The server received ("sr") a request from a client. There is only one request per span.  For
     * example, if duplicate responses were received, each can be logged as a [.WIRE_RECV]
     * annotation.
     *
     *
     * Typically, a trace starts with a server receive, so the first timestamp of a trace is often
     * the timestamp of the root span's server receive.
     *
     *
     * If chunking is involved, each chunk could be logged as a separate [ ][.SERVER_RECV_FRAGMENT] in the same span.
     *
     *
     * [Annotation.endpoint] is not the client. It is the host which logged the receive
     * event, almost always the server. When logging SERVER_RECV, instrumentation should also logger the
     * [.CLIENT_ADDR].
     */
    String SERVER_RECV = "sr";

    /**
     * Message send ("ms") is a request to send a message to a destination, usually a broker. This may
     * be the only annotation in a messaging span. If [.WIRE_SEND] exists in the same span,it
     * follows this moment and clarifies delays sending the message, such as batching.
     *
     *
     * Unlike RPC annotations like [.CLIENT_SEND], messaging spans never share a span ID. For
     * example, "ms" should always be the parent of "mr".
     *
     *
     * [Annotation.endpoint] is not the destination, it is the host which logged the send
     * event: the producer. When annotating MESSAGE_SEND, instrumentation should also tag the [ ][.MESSAGE_ADDR].
     */
    String MESSAGE_SEND = "ms";

    /**
     * A consumer received ("mr") a message from a broker. This may be the only annotation in a
     * messaging span. If [.WIRE_RECV] exists in the same span, it precedes this moment and
     * clarifies any local queuing delay.
     *
     *
     * Unlike RPC annotations like [.SERVER_RECV], messaging spans never share a span ID. For
     * example, "mr" should always be a child of "ms" unless it is a root span.
     *
     *
     * [Annotation.endpoint] is not the broker, it is the host which logged the receive
     * event: the consumer.  When annotating MESSAGE_RECV, instrumentation should also tag the [ ][.MESSAGE_ADDR].
     */
    String MESSAGE_RECV = "mr";

    /**
     * Optionally logs an attempt to send a message on the wire. Multiple wire send events could
     * indicate network retries. A lag between client or server send and wire send might indicate
     * queuing or processing delay.
     */
    String WIRE_SEND = "ws";

    /**
     * Optionally logs an attempt to receive a message from the wire. Multiple wire receive events
     * could indicate network retries. A lag between wire receive and client or server receive might
     * indicate queuing or processing delay.
     */
    String WIRE_RECV = "wr";

}
