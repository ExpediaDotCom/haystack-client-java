package com.expedia.www.haystack.client;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;

import io.opentracing.ActiveSpan;
import io.opentracing.References;

public class SpanPropagationTest {

    private InMemoryDispatcher dispatcher;
    private Tracer tracer;
    private Span span;

    @Before
    public void setUp() throws Exception {
        dispatcher = new InMemoryDispatcher();
        tracer = new Tracer.Builder("TestService", dispatcher).build();
        span = tracer.buildSpan("TestOperation").startManual();
    }

    @Test
    public void testActiveSpan() {
        ActiveSpan active = tracer.buildSpan("active-span").startActive();

        Assert.assertEquals(active, tracer.activeSpan());
    }

    @Test
    public void testActiveSpanPropagation() {
        ActiveSpan activeParent = tracer.buildSpan("active-span").startActive();
        tracer.buildSpan("child-active-span").startActive().deactivate();

        Assert.assertEquals("Haven't closed the parent", 1, dispatcher.getReportedSpans().size());
        Assert.assertEquals(activeParent, tracer.activeSpan());

        activeParent.close();
        Assert.assertEquals("Parent closed", 2, dispatcher.getReportedSpans().size());

        Span child = dispatcher.getReportedSpans().get(0);
        Span parent = dispatcher.getReportedSpans().get(1);

        Assert.assertEquals("Child should have a reference", 1, child.getReferences().size());
        Assert.assertEquals(References.CHILD_OF, child.getReferences().get(0).getReferenceType());
        Assert.assertEquals(parent.context(), child.getReferences().get(0).getContext());
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());
    }

    @Test
    public void testActiveSpanIgnorePropagation() {
        try (ActiveSpan span = tracer.buildSpan("active-span").startActive()) {
            tracer.buildSpan("independent-active-span").ignoreActiveSpan().startActive().deactivate();
        }

        Assert.assertEquals(dispatcher.getReportedSpans().size(), 2);

        Span secondSpan = dispatcher.getReportedSpans().get(0);
        Span firstSpan = dispatcher.getReportedSpans().get(1);

        Assert.assertEquals(0, secondSpan.getReferences().size());
        Assert.assertEquals(0, firstSpan.getReferences().size());
        Assert.assertNotEquals(firstSpan.context().getTraceId(), secondSpan.context().getTraceId());
        Assert.assertEquals(new UUID(0l, 0l), secondSpan.context().getParentId());
    }

    @Test
    public void testActiveSpanPreSeeded() {
        ActiveSpan activeParent = tracer.buildSpan("active").startActive();
        tracer.buildSpan("child").asChildOf(span).startActive().deactivate();
        activeParent.close();
        span.finish();

        Assert.assertEquals("All spans closed", 3, dispatcher.getReportedSpans().size());

        Span child = dispatcher.getReportedSpans().get(0);
        Span active = dispatcher.getReportedSpans().get(1);
        Span parent = dispatcher.getReportedSpans().get(2);

        Assert.assertEquals("Child should have a reference", 1, child.getReferences().size());
        Assert.assertEquals(References.CHILD_OF, child.getReferences().get(0).getReferenceType());
        Assert.assertEquals(parent.context(), child.getReferences().get(0).getContext());
        Assert.assertEquals(parent.context().getTraceId(), child.context().getTraceId());
        Assert.assertEquals(parent.context().getSpanId(), child.context().getParentId());

        Assert.assertTrue(parent.getReferences().isEmpty());
        Assert.assertEquals(new UUID(0l, 0l), parent.context().getParentId());
        Assert.assertTrue(active.getReferences().isEmpty());
        Assert.assertEquals(new UUID(0l, 0l), active.context().getParentId());
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
