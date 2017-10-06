package com.expedia.www.haystack.client;

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.opentracing.References;

public class SpanBuilderTest {

    private Tracer tracer;

    @Before
    public void setUp() throws Exception {
        tracer = new Tracer.Builder("TestService").build();
    }

    @Test
    public void testBasic() {
        Span span = tracer.buildSpan("test-operation").startManual();

        Assert.assertEquals("test-operation", span.getOperatioName());
    }


    @Test
    public void testReferences() {
        Span parent = tracer.buildSpan("parent").startManual();
        Span following = tracer.buildSpan("following").startManual();

        Span child = tracer.buildSpan("child")
            .asChildOf(parent)
            .addReference(References.FOLLOWS_FROM, following.context())
            .startManual();


        Assert.assertEquals(2, child.getReferences().size());
        Assert.assertEquals(parent.context(), child.getReferences().get(0).getContext());
        Assert.assertEquals(References.CHILD_OF, child.getReferences().get(0).getReferenceType());
        Assert.assertEquals(following.context(), child.getReferences().get(1).getContext());
        Assert.assertEquals(References.FOLLOWS_FROM, child.getReferences().get(1).getReferenceType());
    }

    @Test
    public void testWithTags() {
        Span child = tracer.buildSpan("child")
            .withTag("string-key", "string-value")
            .withTag("boolean-key", false)
            .withTag("number-key", 1l)
            .startManual();

        Map<String, ?> tags = child.getTags();

        Assert.assertEquals(3, tags.size());
        Assert.assertTrue(tags.containsKey("string-key"));
        Assert.assertEquals("string-value", tags.get("string-key"));
        Assert.assertTrue(tags.containsKey("boolean-key"));
        Assert.assertEquals(false, tags.get("boolean-key"));
        Assert.assertTrue(tags.containsKey("number-key"));
        Assert.assertEquals(1l, tags.get("number-key"));
    }

}
