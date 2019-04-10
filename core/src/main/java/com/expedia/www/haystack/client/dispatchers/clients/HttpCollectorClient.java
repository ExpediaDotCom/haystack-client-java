/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client.dispatchers.clients;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.expedia.www.haystack.client.dispatchers.formats.ProtoBufFormat;
import com.expedia.www.haystack.remote.clients.BaseHttpClient;
import com.expedia.www.haystack.remote.clients.Client;
import com.expedia.www.haystack.remote.clients.ClientException;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Collections;
import java.util.Map;

public class HttpCollectorClient extends BaseHttpClient implements Client<Span> {
    private final Format<com.expedia.open.tracing.Span> format;

    public HttpCollectorClient(String endpoint, Map<String, String> headers) {
        super(endpoint, headers);
        this.format = new ProtoBufFormat();
    }

    public HttpCollectorClient(String endpoint) {
        this(endpoint, Collections.emptyMap());
    }

    public HttpCollectorClient(final String endpoint,
                               final Map<String, String> headers,
                               final CloseableHttpClient httpClient) {
        super(endpoint, headers, httpClient);
        this.format = new ProtoBufFormat();
    }

    @Override
    public boolean send(Span span) throws ClientException {
        final byte[] spanBytes = format.format(span).toByteArray();
       return super.send(spanBytes);
    }
}