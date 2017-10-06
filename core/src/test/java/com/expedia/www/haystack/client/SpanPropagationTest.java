package com.expedia.www.haystack.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.References;

public class SpanPropagationTest {

    private Tracer tracer;
    private Span span;

    @Before
    public void setUp() throws Exception {
        tracer = new Tracer.Builder("TestService").build();
        span = tracer.buildSpan("TestOperation").startManual();
    }

    @Test
    public void testReferencePropagation() {
        span.setBaggageItem("junk", "trunk");
        span.setBaggageItem("junk2", "trunk2");

        Span child = tracer.buildSpan("childSpan").asChildOf(span).startManual();
        child.setBaggageItem("foo", "baz");

        // parent doesn't get propgated to
        Assert.assertNull(span.getBaggageItem("foo"));

        // child does
        Assert.assertEquals("baz", child.getBaggageItem("foo"));
        Assert.assertEquals("trunk", child.getBaggageItem("junk"));
        Assert.assertEquals("trunk2", child.getBaggageItem("junk2"));
        Assert.assertEquals(span.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(span.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testMultipleParentReferencePropagation() {
        span.setBaggageItem("junk", "trunk");
        span.setBaggageItem("junk2", "trunk2");

        Span parent2 = tracer.buildSpan("parent2").startManual();
        parent2.setBaggageItem("parent", "yes");

        Span child = tracer.buildSpan("childSpan").asChildOf(span).asChildOf(parent2)
            .startManual();
        child.setBaggageItem("foo", "baz");

        // parent doesn't get propgated to
        Assert.assertNull(span.getBaggageItem("foo"));
        Assert.assertNull(parent2.getBaggageItem("foo"));

        // child does
        Assert.assertEquals("baz", child.getBaggageItem("foo"));
        Assert.assertEquals("trunk", child.getBaggageItem("junk"));
        Assert.assertEquals("trunk2", child.getBaggageItem("junk2"));
        Assert.assertEquals("yes", child.getBaggageItem("parent"));
        // first parent added dictates the Ids
        Assert.assertEquals(span.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(span.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testReferencePriorityPropagation() {
        span.setBaggageItem("junk", "trunk");
        span.setBaggageItem("junk2", "trunk2");

        Span parent2 = tracer.buildSpan("parent2").startManual();
        parent2.setBaggageItem("parent", "yes");

        Span ref3 = tracer.buildSpan("ref3").startManual();
        parent2.setBaggageItem("ref3", "ref3");

        Span child = tracer.buildSpan("childSpan")
            .addReference(References.FOLLOWS_FROM, parent2.context())
            .addReference("RANDOM_REF", ref3.context())
            .asChildOf(span)
            .startManual();
        child.setBaggageItem("foo", "baz");

        // parent doesn't get propgated to
        Assert.assertNull(span.getBaggageItem("foo"));
        Assert.assertNull(parent2.getBaggageItem("foo"));
        Assert.assertNull(ref3.getBaggageItem("ref3"));

        // child does
        Assert.assertEquals("baz", child.getBaggageItem("foo"));
        Assert.assertEquals("trunk", child.getBaggageItem("junk"));
        Assert.assertEquals("trunk2", child.getBaggageItem("junk2"));
        Assert.assertEquals("yes", child.getBaggageItem("parent"));
        Assert.assertEquals("ref3", child.getBaggageItem("ref3"));
        // first parent added dictates the Ids
        Assert.assertEquals(span.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(span.context().getSpanId(), child.context().getParentId());
    }

}
