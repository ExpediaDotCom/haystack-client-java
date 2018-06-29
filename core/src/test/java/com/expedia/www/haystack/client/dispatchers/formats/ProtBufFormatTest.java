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
package com.expedia.www.haystack.client.dispatchers.formats;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Tag;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProtBufFormatTest {
    private Tracer tracer;

    private void verifyTag(final Tag tag) {
        switch (tag.getType()) {
            case BOOL:
                assertEquals(true, tag.getVBool());
                break;
            case DOUBLE:
                assertEquals((double) 2, tag.getVDouble(), 0);
                break;
            case LONG:
                assertEquals((long) 1, tag.getVLong());
                break;
            case STRING:
                assertEquals("value1", tag.getVStr());
                break;
            default:
                Assert.fail("Unknown tag encountered");
        }
    }

    @Before
    public void setup() {
        NoopMetricsRegistry metrics = new NoopMetricsRegistry();
        tracer = new Tracer.Builder(metrics, "protobuf-format-tests", new InMemoryDispatcher.Builder(metrics).build()).build();
    }

    @Test
    public void testLogTypes() throws Exception {
        Span span = tracer.buildSpan("log-types").start();
        span.log(ImmutableMap.<String, Object>builder()
                 .put("string", "value1")
                 .put("boolean", true)
                 .put("long", 1L)
                 .put("int", 1)
                 .put("short", Short.parseShort("1"))
                 .put("double", Double.valueOf(2d))
                 .put("float", Float.parseFloat("2.0000"))
                 .build());

        com.expedia.open.tracing.Span protoSpan = new ProtoBufFormat().format(span);
        assertEquals(1, protoSpan.getLogsCount());

        Log log = protoSpan.getLogs(0);
        assertEquals(7, log.getFieldsCount());

        for (final Tag tag : log.getFieldsList()) {
            verifyTag(tag);
        }
    }

    @Test
    public void testTagTypes() {
        Span span = tracer.buildSpan("tag-types").start();
        span.setTag("string", "value1");
        span.setTag("boolean", true);
        span.setTag("long", (long) 1);
        span.setTag("int", 1);
        span.setTag("short", Short.parseShort("1"));
        span.setTag("double", Double.valueOf(2d));
        span.setTag("float", Float.parseFloat("2.0000"));

        com.expedia.open.tracing.Span protoSpan = new ProtoBufFormat().format(span);
        assertEquals(7, protoSpan.getTagsCount());

        for (final Tag tag : protoSpan.getTagsList()) {
            verifyTag(tag);
        }
    }

    @Test
    public void testChildSpanConversion() {
        Span parent = tracer.buildSpan("parent")
            .withStartTimestamp(1L).start();
        parent.setBaggageItem("parent-baggage", "value");

        Span child = tracer.buildSpan("child").asChildOf(parent)
            .withStartTimestamp(2L).start();
        child.finish(4);
        parent.finish(6);

        com.expedia.open.tracing.Span protoSpan = new ProtoBufFormat().format(child);

        assertEquals("protobuf-format-tests", protoSpan.getServiceName());
        assertEquals("child", protoSpan.getOperationName());

        assertEquals(2, protoSpan.getStartTime());
        assertEquals(2, protoSpan.getDuration());

        assertEquals(0, protoSpan.getLogsCount());
        // Tags + Baggage for now.
        assertEquals(1, protoSpan.getTagsCount());

        // best we can do for now; when relationships get propgated revisit
        assertEquals(protoSpan.getParentSpanId(), parent.context().getSpanId().toString());
    }

    @Test
    public void testSpanConversion() {
        Span span = tracer.buildSpan("happy-path").
            withStartTimestamp(1).start();
        span.log("simple-event");
        span.log(ImmutableMap.of("key1", "value1", "key2", "value2"));
        span.setBaggageItem("baggage", "value");
        span.setTag("tag1", "value1");
        span.finish(2);

        com.expedia.open.tracing.Span protoSpan = new ProtoBufFormat().format(span);

        assertEquals("protobuf-format-tests", protoSpan.getServiceName());
        assertEquals("happy-path", protoSpan.getOperationName());

        assertNotNull(protoSpan.getTraceId());
        assertNotNull(protoSpan.getSpanId());
        assertNotNull(protoSpan.getParentSpanId());

        assertEquals(1, protoSpan.getStartTime());
        assertEquals(1, protoSpan.getDuration());

        assertEquals(2, protoSpan.getLogsCount());

        // Tags + Baggage for now.
        assertEquals(2, protoSpan.getTagsCount());
    }
}
