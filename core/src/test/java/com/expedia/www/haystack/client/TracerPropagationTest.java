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
package com.expedia.www.haystack.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;

public class TracerPropagationTest {
    private Tracer tracer;

    @Before
    public void setUp() throws Exception {
        tracer = new Tracer.Builder(new NoopMetricsRegistry(), "TestTracer", new NoopDispatcher()).build();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testInjectInvalidFormat() {
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        String carrier = new String();

        SpanContext context = new SpanContext(traceId, spanId, parentId).addBaggage("TEST", "TEXT");

        tracer.inject(context, new Format<String>() {}, carrier);
    }

    @Test
    public void testInject() {
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Map<String, String> carrierValues = new HashMap<>();
        TextMap carrier = new TextMapInjectAdapter(carrierValues);

        SpanContext context = new SpanContext(traceId, spanId, parentId).addBaggage("TEST", "TEXT");

        tracer.inject(context, Format.Builtin.TEXT_MAP, carrier);

        Assert.assertEquals(carrierValues.size(), 4);
        Assert.assertEquals(carrierValues.get("Trace-ID"), traceId.toString());
        Assert.assertEquals(carrierValues.get("Span-ID"), spanId.toString());
        Assert.assertEquals(carrierValues.get("Parent-ID"), parentId.toString());
        Assert.assertEquals(carrierValues.get("Baggage-TEST"), "TEXT");

    }

    @Test
    public void testInjectURLEncoded() {
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Map<String, String> carrierValues = new HashMap<>();
        TextMap carrier = new TextMapInjectAdapter(carrierValues);

        SpanContext context = new SpanContext(traceId, spanId, parentId).addBaggage("TEST", "!@##*^ %^&&(*").addBaggage("!@##*^ %^&&(*", "TEXT");

        tracer.inject(context, Format.Builtin.HTTP_HEADERS, carrier);

        Assert.assertEquals(carrierValues.size(), 5);
        Assert.assertEquals(carrierValues.get("Trace-ID"), traceId.toString());
        Assert.assertEquals(carrierValues.get("Span-ID"), spanId.toString());
        Assert.assertEquals(carrierValues.get("Parent-ID"), parentId.toString());
        Assert.assertEquals(carrierValues.get("Baggage-TEST"), "%21%40%23%23*%5E+%25%5E%26%26%28*");
        Assert.assertEquals(carrierValues.get("Baggage-%21%40%23%23*%5E+%25%5E%26%26%28*"), "TEXT");

    }

    @Test
    public void testExtract() {
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Map<String, String> carrierValues = new HashMap<>();
        carrierValues.put("Baggage-TEST", "TEXT");
        carrierValues.put("Trace-ID", traceId.toString());
        carrierValues.put("Span-ID", spanId.toString());
        carrierValues.put("Parent-ID", parentId.toString());

        TextMap carrier = new TextMapExtractAdapter(carrierValues);

        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, carrier);

        Assert.assertEquals(context.getTraceId(), traceId);
        Assert.assertEquals(context.getSpanId(), spanId);
        Assert.assertEquals(context.getParentId(), parentId);
        Assert.assertEquals(context.getBaggage().size(), 1);
        Assert.assertEquals(context.getBaggageItem("TEST"), "TEXT");
    }

    @Test
    public void testExtractIgnoreUnknowns() {
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Map<String, String> carrierValues = new HashMap<>();
        carrierValues.put("Trace-ID", traceId.toString());
        carrierValues.put("Span-ID", spanId.toString());
        carrierValues.put("Parent-ID", parentId.toString());

        carrierValues.put("JunkKey", parentId.toString());
        carrierValues.put("JunkKey2", parentId.toString());

        TextMap carrier = new TextMapExtractAdapter(carrierValues);

        SpanContext context = tracer.extract(Format.Builtin.HTTP_HEADERS, carrier);

        Assert.assertEquals(context.getTraceId(), traceId);
        Assert.assertEquals(context.getSpanId(), spanId);
        Assert.assertEquals(context.getParentId(), parentId);
        Assert.assertEquals(context.getBaggage().size(), 0);
    }

    @Test
    public void testExtractInvalid() {
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Map<String, String> carrierValues = new HashMap<>();
        carrierValues.put("Span-ID", spanId.toString());
        carrierValues.put("Parent-ID", parentId.toString());

        TextMap carrier = new TextMapExtractAdapter(carrierValues);

        SpanContext context = tracer.extract(Format.Builtin.HTTP_HEADERS, carrier);

        Assert.assertEquals(context, null);
    }

    @Test
    public void testExtractURLEncoded() {
        UUID traceId = UUID.randomUUID();
        UUID spanId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Map<String, String> carrierValues = new HashMap<>();
        carrierValues.put("Baggage-TEST", "!%40%23%23*%5E%20%25%5E%26%26(*");
        carrierValues.put("Baggage-!%40%23%23*%5E%20%25%5E%26%26(*", "TEST");
        carrierValues.put("Trace-ID", traceId.toString());
        carrierValues.put("Span-ID", spanId.toString());
        carrierValues.put("Parent-ID", parentId.toString());

        TextMap carrier = new TextMapExtractAdapter(carrierValues);

        SpanContext context = tracer.extract(Format.Builtin.HTTP_HEADERS, carrier);

        Assert.assertEquals(context.getTraceId(), traceId);
        Assert.assertEquals(context.getSpanId(), spanId);
        Assert.assertEquals(context.getParentId(), parentId);
        Assert.assertEquals(context.getBaggage().size(), 2);
        Assert.assertEquals(context.getBaggageItem("TEST"), "!@##*^ %^&&(*");
        Assert.assertEquals(context.getBaggageItem("!@##*^ %^&&(*"), "TEST");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testExtractInvalidFormat() {
        tracer.extract(new Format<String>() {}, new String());
    }


}
