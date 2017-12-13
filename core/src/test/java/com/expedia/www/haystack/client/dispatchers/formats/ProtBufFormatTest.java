package com.expedia.www.haystack.client.dispatchers.formats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.open.tracing.Log;
import com.expedia.open.tracing.Tag;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.google.common.collect.ImmutableMap;

public class ProtBufFormatTest {
    private Tracer tracer;

    @Before
    public void setup() {
        tracer = new Tracer.Builder("protobuf-format-tests", new InMemoryDispatcher())
            .build();
    }

    @Test
    public void testLogTypes() throws Exception {
        Span span = tracer.buildSpan("log-types").startManual();
        span.log(ImmutableMap.of("string", "value1",
                                 "boolean", true,
                                 "long", (long) 1,
                                 "double", (double) 2));

        com.expedia.open.tracing.Span protoSpan = new ProtoBufFormat().format(span);
        assertEquals(1, protoSpan.getLogsCount());

        Log log = protoSpan.getLogs(0);
        assertEquals(4, log.getFieldsCount());

        for (Tag tag : log.getFieldsList()) {
            switch (tag.getType()) {
            case BOOL:
                assertEquals(Boolean.valueOf(true), tag.getVBool());
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
    }

    @Test
    public void testTagTypes() {
        Span span = tracer.buildSpan("tag-types").startManual();
        span.setTag("string", "value1");
        span.setTag("boolean", true);
        span.setTag("long", (long) 1);
        span.setTag("double", (double) 2);

        com.expedia.open.tracing.Span protoSpan = new ProtoBufFormat().format(span);
        assertEquals(4, protoSpan.getTagsCount());

        for (Tag tag : protoSpan.getTagsList()) {
            switch (tag.getType()) {
            case BOOL:
                assertEquals(Boolean.valueOf(true), tag.getVBool());
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
    }

    @Test
    public void testChildSpanConversion() {
        Span parent = tracer.buildSpan("parent")
            .withStartTimestamp(1L).startManual();
        parent.setBaggageItem("parent-baggage", "value");

        Span child = tracer.buildSpan("child").asChildOf(parent)
            .withStartTimestamp(2L).startManual();
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
            withStartTimestamp(1).startManual();
        span.log("simple-event");
        span.log("event", "payload");
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

        assertEquals(3, protoSpan.getLogsCount());

        // Tags + Baggage for now.
        assertEquals(2, protoSpan.getTagsCount());
    }

}
