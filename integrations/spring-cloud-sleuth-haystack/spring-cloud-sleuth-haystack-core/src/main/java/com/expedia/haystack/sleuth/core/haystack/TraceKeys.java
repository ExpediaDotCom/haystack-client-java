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

/**
 * To help with tracking interactions between services and tracing them all the way to the originating client (browser),
 * Systems in Expedia logger and pass around few identifiers (as headers) in our service interactions. This file defines those
 * identifiers as constants
 *
 * @see [Haystack](https://dictionary.exp-tools.net/v/haystack)
 *
 * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
 */
public interface TraceKeys {

    /**
     * Services receiving a request with Span-ID (type: UUID) in the header (or TransactionGUID in the message) are expected to
     * * Log the identifier for easier correlation of logs associated with servicing this specific request and
     * * Pass the identifier on all downstream calls as Span-ID
     * When a request is received without this header, unless the message carries a TransactionGUID, services are
     * expected to generate a new UUID and pass it on all future downstream calls as Span-ID.
     * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
     */
    String TRACE_ID = "Span-ID";

    /**
     * Unique identifier created by the consumer of the service for a specific invocation. Same purpose as MessageGUID in E3 messages.
     * Services using E3 message schema should expect Message-ID and MessageGUID be the same. Message-ID should never be reused.
     * Multiple calls to a service by a calling service should generate distinct UUIDs for each call. Services that receive a
     * request with Message-ID header are expected to add this identifier to the entries logged as part of processing the request.
     * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
     */
    String PARENT_ID = "Message-ID";

    /**
     * Unique identifier associated with the service making this request. i.e., Message-ID of the consumer or parent of the current
     * call.
     * It is highly recommended that each service send the Message-ID received by the service as Parent-Message-ID in the downstream
     * calls. This identifier can be used to build the call tree in an effective manner by systems like Haystack
     * (or by other OpenTracing compliant services)
     * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
     */
    String PARENT_MESSAGE_ID = "Parent-Message-ID";

    /**
     * Presence of this header indicates that the current request is in debug mode. Service receiving this header is expected to
     * send the header in all future downstream calls. In addition, the service should also logger the entire request and response (aka BLOBs) in
     * Haystack (or CRS Log) or equivalent system for debugging.
     * @see [Haystack](https://dictionary.exp-tools.net/v/haystack)
     *
     * @see [CRS](https://dictionary.exp-tools.net/v/crs)
     *
     * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
     */
    String DEBUG_TRACE = "Debug-Span";

    /**
     * Unique identifier of the originating client (browser or an app). It is also knows as MC1 or UserGUID in our systems. Any
     * service can use Watson APIs to get a consolidated profile of the traveler associated with this ID. Services are also
     * expected to add this header to all downstream calls.
     *
     * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
     */
    String DEVICE_USERAGENT_ID = "Device-User-Agent-ID";

    /**
     * Used for conveying client's identity for logging purposes only
     * Type: Comma Separated Value. Value: Application-Name,Application-Version,Application-Location. Three parts separated by
     * commas. Name of the calling service; version of the calling service; location of the calling service. For example,
     * Client-Info header from Expweb instance running in AWS us-west-2 might look like ‘expweb,release-2017-04-r4.5721.1803793,us-west-2′
     * @see [Techtrain post on tracing](http://blog/techtrain/2017/06/22/tracking-requests-diagnostic-logging-bex/)
     */
    String CLIENT_INFO = "Client-Info";
}
