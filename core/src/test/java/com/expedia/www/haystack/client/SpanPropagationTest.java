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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;

import io.opentracing.References;
import io.opentracing.Scope;

public class SpanPropagationTest {

    private InMemoryDispatcher dispatcher;
    private Tracer tracer;

    @Before
    public void setUp() throws Exception {
        NoopMetricsRegistry metrics = new NoopMetricsRegistry();
        dispatcher = new InMemoryDispatcher.Builder(metrics).build();
        tracer = new Tracer.Builder(metrics, "TestService", dispatcher).build();
    }

    @Test
    public void testActiveSpan() {
        Assert.assertNull(tracer.activeSpan());

        try (Scope scope = tracer.buildSpan("active-span").startActive(true)) {
            Assert.assertEquals(tracer.scopeManager().active().span(), tracer.activeSpan());
            Assert.assertEquals(scope.span(), tracer.activeSpan());
        }

        Assert.assertNull(tracer.activeSpan());
    }

    @Test
    public void testActiveSpanPropagation() {
        Assert.assertNull(tracer.activeSpan());

        try (Scope scope = tracer.buildSpan("active-span").startActive(true)) {
            tracer.buildSpan("child-active-span").start().finish();

            Assert.assertEquals(tracer.scopeManager().active().span(), tracer.activeSpan());
            Assert.assertEquals(scope.span(), tracer.activeSpan());
            Assert.assertEquals("Haven't closed the parent", 1, dispatcher.getReportedSpans().size());
        }

        Assert.assertEquals("Parent closed", 2, dispatcher.getReportedSpans().size());

        Span child = dispatcher.getReportedSpans().get(0);
        Span parent = dispatcher.getReportedSpans().get(1);

        Assert.assertEquals("Child should have a reference", 1, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference(References.CHILD_OF, parent.context()));
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testActiveSpanIgnorePropagation() {
        try (Scope scope = tracer.buildSpan("active-span").startActive(true)) {
            Span seperateSpan = tracer.buildSpan("independent-active-span").ignoreActiveSpan().start();
            seperateSpan.finish();
        }

        Assert.assertEquals(2, dispatcher.getReportedSpans().size());

        Span secondSpan = dispatcher.getReportedSpans().get(0);
        Span firstSpan = dispatcher.getReportedSpans().get(1);

        Assert.assertEquals(0, secondSpan.getReferences().size());
        Assert.assertEquals(0, firstSpan.getReferences().size());
        Assert.assertNotEquals(firstSpan.context().getTraceId(), secondSpan.context().getTraceId());
        Assert.assertNull(secondSpan.context().getParentId());
    }

    @Test
    public void testActiveSpanPreSeeded() {
        Span parentSpan = tracer.buildSpan("parent").start();

        try (Scope scope = tracer.buildSpan("active").startActive(true)) {
            tracer.buildSpan("child").asChildOf(parentSpan).start().finish();
        }
        parentSpan.finish();

        Assert.assertEquals("All spans closed", 3, dispatcher.getReportedSpans().size());

        Span child = dispatcher.getReportedSpans().get(0);
        Span active = dispatcher.getReportedSpans().get(1);
        Span parent = dispatcher.getReportedSpans().get(2);

        Assert.assertEquals("Child should have a reference", 1, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference(References.CHILD_OF, parent.context()));
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());

        Assert.assertTrue(parent.getReferences().isEmpty());
        Assert.assertNull(parent.context().getParentId());
        Assert.assertTrue(active.getReferences().isEmpty());
        Assert.assertNull(active.context().getParentId());
    }

    @Test
    public void testReferencePropagation() {
        Span parent = tracer.buildSpan("parent").start();
        parent.setBaggageItem("junk", "trunk");
        parent.setBaggageItem("junk2", "trunk2");

        Span child = tracer.buildSpan("childSpan").asChildOf(parent).start();
        child.setBaggageItem("foo", "baz");

        // parent doesn't get propgated to
        Assert.assertNull(parent.getBaggageItem("foo"));

        // child does
        Assert.assertEquals("baz", child.getBaggageItem("foo"));
        Assert.assertEquals("trunk", child.getBaggageItem("junk"));
        Assert.assertEquals("trunk2", child.getBaggageItem("junk2"));
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testMultipleParentReferencePropagation() {
        Span parent = tracer.buildSpan("parent").start();
        parent.setBaggageItem("junk", "trunk");
        parent.setBaggageItem("junk2", "trunk2");

        Span parent2 = tracer.buildSpan("parent2").start();
        parent2.setBaggageItem("parent", "yes");

        Span child = tracer.buildSpan("childSpan").asChildOf(parent).asChildOf(parent2)
            .start();
        child.setBaggageItem("foo", "baz");

        // parent doesn't get propgated to
        Assert.assertNull(parent.getBaggageItem("foo"));
        Assert.assertNull(parent2.getBaggageItem("foo"));

        // child does
        Assert.assertEquals("baz", child.getBaggageItem("foo"));
        Assert.assertEquals("trunk", child.getBaggageItem("junk"));
        Assert.assertEquals("trunk2", child.getBaggageItem("junk2"));
        Assert.assertEquals("yes", child.getBaggageItem("parent"));
        // first parent added dictates the Ids
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testReferencePriorityPropagation() {
        Span parent = tracer.buildSpan("parent").start();
        parent.setBaggageItem("junk", "trunk");
        parent.setBaggageItem("junk2", "trunk2");

        Span parent2 = tracer.buildSpan("parent2").start();
        parent2.setBaggageItem("parent", "yes");

        Span ref3 = tracer.buildSpan("ref3").start();
        parent2.setBaggageItem("ref3", "ref3");

        Span child = tracer.buildSpan("childSpan")
            .addReference(References.FOLLOWS_FROM, parent2.context())
            .addReference("RANDOM_REF", ref3.context())
            .asChildOf(parent)
            .start();
        child.setBaggageItem("foo", "baz");

        // parent doesn't get propgated to
        Assert.assertNull(parent.getBaggageItem("foo"));
        Assert.assertNull(parent2.getBaggageItem("foo"));
        Assert.assertNull(ref3.getBaggageItem("ref3"));

        // child does
        Assert.assertEquals("baz", child.getBaggageItem("foo"));
        Assert.assertEquals("trunk", child.getBaggageItem("junk"));
        Assert.assertEquals("trunk2", child.getBaggageItem("junk2"));
        Assert.assertEquals("yes", child.getBaggageItem("parent"));
        Assert.assertEquals("ref3", child.getBaggageItem("ref3"));
        // first parent added dictates the Ids
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testMultiReferences() {
        final Span parent = tracer.buildSpan("parent").start();
        final Span followsFrom = tracer.buildSpan("followsFrom").start();

        final Span child = tracer.buildSpan("child")
            .addReference(References.FOLLOWS_FROM, followsFrom.context())
            .asChildOf(parent)
            .start();

        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
        Assert.assertEquals(2, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference(References.FOLLOWS_FROM, followsFrom.context()));
        Assert.assertEquals(child.getReferences().get(1), new Reference(References.CHILD_OF, parent.context()));
    }

    @Test
    public void testFollowsFromReference() {
        final Span parent = tracer.buildSpan("parent").start();

        final Span child = tracer.buildSpan("follows")
            .addReference(References.FOLLOWS_FROM, parent.context())
            .start();

        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
        Assert.assertEquals(1, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference(References.FOLLOWS_FROM, parent.context()));
    }


    @Test
    public void testNonStandardReference() {
        final Span parent = tracer.buildSpan("parent").start();

        final Span child = tracer.buildSpan("follows")
            .addReference("RANDOM_REF", parent.context())
            .start();

        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
        Assert.assertEquals(1, child.getReferences().size());
        Assert.assertEquals(child.getReferences().get(0), new Reference("RANDOM_REF", parent.context()));
    }

    @Test
    public void testChildOfWithNullParentDoesNotThrowException() {
        final Span parent = null;
        final Span span = tracer.buildSpan("foo").asChildOf(parent).start();
        span.finish();
    }
}
