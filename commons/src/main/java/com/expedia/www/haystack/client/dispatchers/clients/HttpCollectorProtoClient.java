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

import com.expedia.open.tracing.Span;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Map;

public class HttpCollectorProtoClient extends BaseHttpClient implements Client<Span>{
    public HttpCollectorProtoClient(String endpoint, Map<String, String> headers) {
        super(endpoint, headers);
    }

    public HttpCollectorProtoClient(String endpoint) {
        super(endpoint);
    }

    public HttpCollectorProtoClient(String endpoint, Map<String, String> headers, CloseableHttpClient httpClient) {
        super(endpoint, headers, httpClient);
    }

    @Override
    public boolean send(Span span) throws ClientException {
        return super.send(span.toByteArray());
    }
}
