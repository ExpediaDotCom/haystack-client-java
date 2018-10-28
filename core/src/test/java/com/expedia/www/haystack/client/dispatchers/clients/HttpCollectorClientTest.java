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
import com.expedia.www.haystack.client.SpanContext;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class HttpCollectorClientTest {

    private Tracer tracer;
    private final static String serviceName = "dummy-service";
    private final static UUID traceId = UUID.randomUUID();
    private final static UUID spanId = UUID.randomUUID();
    private final static SpanContext spanContext = new SpanContext(traceId, spanId, null);

    @Before
    public void setup() throws Exception {
        NoopMetricsRegistry metrics = new NoopMetricsRegistry();
        tracer = new Tracer.Builder(metrics, serviceName, new InMemoryDispatcher.Builder(metrics).build()).build();
    }

    @After
    public void teardown() throws Exception {
        tracer.close();
    }

    @Test
    public void testDispatch() throws Exception {
        final Span span = tracer.buildSpan("happy-path").asChildOf(spanContext).start();
        span.finish();
        final ArgumentCaptor<HttpPost> httpPostCapture = ArgumentCaptor.forClass(HttpPost.class);

        final CloseableHttpClient http = Mockito.mock(CloseableHttpClient.class);
        final CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        final BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("v", 1, 1), 200, "");

        when(http.execute(httpPostCapture.capture())).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        final Map<String, String> headers = new HashMap<>();
        headers.put("client-id", "my-client");

        final HttpCollectorClient client = new HttpCollectorClient("http://myendpoint:8080/span", headers, http);
        final boolean isSuccess = client.send(span);
        client.close();
        verify(http, times(1)).execute(httpPostCapture.capture());
        verify(httpResponse, times(1)).close();
        verify(http, times(1)).close();
        verifyCapturedHttpPost(httpPostCapture.getValue());
        assertEquals(isSuccess, true);
    }

    @Test(expected = ClientException.class)
    public void testFailedDispatch() throws Exception {
        final Span span = tracer.buildSpan("sad-path").start();
        span.finish();
        final ArgumentCaptor<HttpPost> httpPostCapture = ArgumentCaptor.forClass(HttpPost.class);
        final CloseableHttpClient http = Mockito.mock(CloseableHttpClient.class);
        final CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        final BasicStatusLine statusLine = new BasicStatusLine(new ProtocolVersion("v", 1, 1), 404, "");

        when(http.execute(httpPostCapture.capture())).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        final HttpCollectorClient client = new HttpCollectorClient("http://myendpoint:8080/span", new HashMap<>(), http);
        client.send(span);
    }

    private void verifyCapturedHttpPost(final HttpPost httpPost) throws IOException {
        assertEquals(httpPost.getMethod(), "POST");
        assertEquals(httpPost.getURI().toString(), "http://myendpoint:8080/span");
        assertEquals(httpPost.getFirstHeader("Content-Type").getValue(), "application/octet-stream");
        assertEquals(httpPost.getFirstHeader("client-id").getValue(), "my-client");
        com.expedia.open.tracing.Span span = com.expedia.open.tracing.Span.parseFrom(httpPost.getEntity().getContent());
        assertEquals(span.getServiceName(), serviceName);
        assertEquals(span.getOperationName(), "happy-path");
        assertEquals(span.getTraceId(), traceId.toString());
        assertEquals(span.getParentSpanId(), spanId.toString());
    }
}
