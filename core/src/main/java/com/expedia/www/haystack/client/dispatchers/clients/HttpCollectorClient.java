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
import org.apache.commons.lang3.Validate;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpCollectorClient implements Client {
    private final Format<com.expedia.open.tracing.Span> format;
    private final String endpoint;
    private final BasicHeader[] headers;
    private final CloseableHttpClient httpClient;

    public HttpCollectorClient(final String endpoint,
                               final Map<String, String> headers) {
       this(endpoint, headers, HttpClients.createDefault());
    }

    public HttpCollectorClient(final String endpoint) {
        this(endpoint, new HashMap<>());
    }

    public HttpCollectorClient(final String endpoint,
                               final Map<String, String> headers,
                               final CloseableHttpClient httpClient) {
        Validate.notEmpty(endpoint, "Haystack collector endpoint can't be empty");

        this.format = new ProtoBufFormat();
        this.endpoint = endpoint;
        this.httpClient = httpClient;
        this.headers = headers.entrySet().stream()
                .map((entry) -> new BasicHeader(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList())
                .toArray(new BasicHeader[0]);
    }

    @Override
    public void close() throws ClientException {
        closeQuietly(httpClient);
    }

    @Override
    public void flush() throws ClientException {
        /* no batching, no flushing */
    }

    @Override
    public boolean send(Span span) throws ClientException {
        final byte[] spanBytes = format.format(span).toByteArray();

        final HttpPost post = new HttpPost(endpoint);
        if (headers != null && headers.length > 0) {
            post.setHeaders(headers);
        }
        post.setHeader(new BasicHeader("Content-Type", "application/octet-stream"));
        post.setEntity(new ByteArrayEntity(spanBytes));
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(post);
            final int statusCode = getStatusCode(response);
            if (is2xx(statusCode)) {
                return true;
            }
            throw new ClientException(String.format("Failed sending span to http collector endpoint=%s, http status code=%d", endpoint, statusCode));
        } catch (IOException e) {
            throw new ClientException(String.format("Failed sending span to http collector endpoint=%s", endpoint), e);
        } finally {
            closeQuietly(response);
        }
    }

    private int getStatusCode(final CloseableHttpResponse response) {
        return response.getStatusLine() == null ? 0 : response.getStatusLine().getStatusCode();
    }

    private boolean is2xx(final int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    public Header[] getHeaders() {
        return headers;
    }
    public String getEndpoint() {
        return endpoint;
    }

    private void closeQuietly(final Closeable obj) {
        try {
            obj.close();
        } catch (Exception ex) {
            /* be quiet */
        }
    }
}