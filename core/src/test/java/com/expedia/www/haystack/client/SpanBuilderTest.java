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

import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import com.expedia.www.haystack.client.propagation.MapBackedTextMap;
import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SpanBuilderTest {

    private Dispatcher dispatcher;
    private Tracer tracer;

    @Before
    public void setUp() {
        dispatcher = new NoopDispatcher();
        tracer = new Tracer.Builder(new NoopMetricsRegistry(), "TestService", dispatcher).build();
    }

    @Test
    public void testBasic() {
        Span span = tracer.buildSpan("test-operation").start();

        Assert.assertEquals("test-operation", span.getOperationName());
    }


    @Test
    public void testReferences() {
        Span parent = tracer.buildSpan("parent").start();
        Span following = tracer.buildSpan("following").start();

        Span child = tracer.buildSpan("child")
                .asChildOf(parent)
                .addReference(References.FOLLOWS_FROM, following.context())
                .start();

        Assert.assertEquals(2, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference(References.CHILD_OF, parent.context()));
        Assert.assertEquals(child.getReferences().get(1), new Reference(References.FOLLOWS_FROM, following.context()));
    }

    @Test
    public void testChildOfWithDualSpanType() {
        //create a client span
        final Tracer clientTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ClientService",
                                                       dispatcher).withDualSpanMode().build();
        final Span clientSpan = clientTracer.buildSpan("Api_call")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .start();
        final MapBackedTextMap wireData = new MapBackedTextMap();
        clientTracer.inject(clientSpan.context(), Format.Builtin.TEXT_MAP, wireData);

        //create a server
        final Tracer serverTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ServerService",
                                                       dispatcher).withDualSpanMode().build();
        final SpanContext wireContext = serverTracer.extract(Format.Builtin.TEXT_MAP, wireData);
        final Span serverSpan = serverTracer.buildSpan("Api")
                .asChildOf(wireContext)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .start();

        Assert.assertEquals("trace-ids are not matching",
                            clientSpan.context().getTraceId().toString(),
                            serverSpan.context().getTraceId().toString());
        Assert.assertEquals("server's parent id - client's span id do not match",
                            clientSpan.context().getSpanId().toString(),
                            serverSpan.context().getParentId().toString());
    }

    @Test
    public void testChildOfWithSingleSpanType() {
        //create a client span
        final Tracer clientTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ClientService",
                                                       dispatcher).build();
        final Span clientSpan = clientTracer.buildSpan("Api_call")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .start();
        final MapBackedTextMap wireData = new MapBackedTextMap();
        clientTracer.inject(clientSpan.context(), Format.Builtin.TEXT_MAP, wireData);

        //create a server
        final Tracer serverTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ServerService",
                                                       dispatcher).build();
        final SpanContext wireContext = serverTracer.extract(Format.Builtin.TEXT_MAP, wireData);
        final Span serverSpan = serverTracer.buildSpan("Api")
                .asChildOf(wireContext)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .start();

        Assert.assertEquals("trace-ids are not matching",
                            clientSpan.context().getTraceId().toString(),
                            serverSpan.context().getTraceId().toString());
        Assert.assertEquals("server - client spans do not match",
                            clientSpan.context().getSpanId().toString(),
                            serverSpan.context().getSpanId().toString());
    }


    @Test
    public void testChildOfWithSingleSpanTypeAndExtractedContext() {
        //create a client span
        final Tracer clientTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ClientService",
                                                       dispatcher).build();
        final Span clientSpan = clientTracer.buildSpan("Api_call")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .start();
        final MapBackedTextMap wireData = new MapBackedTextMap();
        clientTracer.inject(clientSpan.context(), Format.Builtin.TEXT_MAP, wireData);

        //create a server
        final Tracer serverTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ServerService",
                                                       dispatcher).build();
        final SpanContext wireContext = serverTracer.extract(Format.Builtin.TEXT_MAP, wireData);
        final Span serverSpan = serverTracer.buildSpan("Api")
                .asChildOf(wireContext)
                .start();

        Assert.assertEquals("trace-ids are not matching",
                            clientSpan.context().getTraceId().toString(),
                            serverSpan.context().getTraceId().toString());
        Assert.assertEquals("server - client spans do not match",
                            clientSpan.context().getSpanId().toString(),
                            serverSpan.context().getSpanId().toString());
    }

    @Test
    public void testChildOfSingleSpanTypeWithExtractedContextDoesNotPropagateExtractedContext() {
        //create a client span
        final Tracer clientTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ClientService",
                                                       dispatcher).build();
        final Span clientSpan = clientTracer.buildSpan("Api_call")
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .start();
        final MapBackedTextMap wireData = new MapBackedTextMap();
        clientTracer.inject(clientSpan.context(), Format.Builtin.TEXT_MAP, wireData);

        //create a server
        final Tracer serverTracer = new Tracer.Builder(new NoopMetricsRegistry(),
                                                       "ServerService",
                                                       dispatcher).build();
        final SpanContext wireContext = serverTracer.extract(Format.Builtin.TEXT_MAP, wireData);
        final Span serverSpan = serverTracer.buildSpan("Api")
                .asChildOf(wireContext)
                .start();

        //make sure client and server have the same span-ids for single span type
        Assert.assertEquals("trace-ids are not matching",
                            clientSpan.context().getTraceId().toString(),
                            serverSpan.context().getTraceId().toString());
        Assert.assertEquals("server - client span-ids do not match",
                            clientSpan.context().getSpanId().toString(),
                            serverSpan.context().getSpanId().toString());

        //not create another child of server span
        final Span childOfServerSpan = serverTracer.buildSpan("child_operation")
                .asChildOf(serverSpan)
                .start();

        //make sure server span and child span have the same trace-ids but different span-ids
        Assert.assertEquals("trace-ids are not matching",
                            serverSpan.context().getTraceId().toString(),
                            childOfServerSpan.context().getTraceId().toString());
        Assert.assertNotEquals("server - childOfServerSpan span-ids match - they should not",
                            serverSpan.context().getSpanId().toString(),
                            childOfServerSpan.context().getSpanId().toString());
        Assert.assertEquals("server's span id - childOfServerSpan's parent id do not match",
                            serverSpan.context().getSpanId().toString(),
                            childOfServerSpan.context().getParentId().toString());
    }

    @Test
    public void testWithTags() {
        Span child = tracer.buildSpan("child")
                .withTag("string-key", "string-value")
                .withTag("boolean-key", false)
                .withTag("number-key", 1L)
                .start();

        Map<String, ?> tags = child.getTags();

        Assert.assertEquals(3, tags.size());
        Assert.assertTrue(tags.containsKey("string-key"));
        Assert.assertEquals("string-value", tags.get("string-key"));
        Assert.assertTrue(tags.containsKey("boolean-key"));
        Assert.assertEquals(false, tags.get("boolean-key"));
        Assert.assertTrue(tags.containsKey("number-key"));
        Assert.assertEquals(1L, tags.get("number-key"));
    }
}
