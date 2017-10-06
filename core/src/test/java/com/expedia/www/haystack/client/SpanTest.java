package com.expedia.www.haystack.client;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SpanTest {

    private Tracer tracer;
    private Span span;

    @Before
    public void setUp() throws Exception {
        tracer = new Tracer.Builder("TestService").build();
        span = tracer.buildSpan("TestOperation").startManual();
    }

    @Test
    public void testDuration() {
        String expected = "op-name";
        Span span = tracer.buildSpan(expected).withStartTimestamp(1l).startManual();

        span.finish(2l);

        Assert.assertEquals((Long) 1l, span.getStartTime());
        Assert.assertEquals((Long) 2l, span.getEndTime());
        Assert.assertEquals((Long) 1l, span.getDuration());
    }

    @Test
    public void testOperationName() {
        String expected = "op-name";
        String modString = "other-op-name";
        Span span = tracer.buildSpan(expected).startManual();
        Assert.assertEquals(expected, span.getOperatioName());
        span.setOperationName(modString);
        Assert.assertEquals(modString, span.getOperatioName());
    }

    @Test
    public void testServiceName() {
        String expected = "service-name";
        Span span = new Tracer.Builder(expected).build().buildSpan(expected).startManual();
        Assert.assertEquals(expected, span.getServiceName());
    }

    @Test
    public void testBaggage() {
        String key = "key-name";
        String value = "value-value";
        span.setBaggageItem(key, value);
        Assert.assertEquals(value, span.getBaggageItem(key));
    }

    @Test
    public void testTagForNumber() {
        String key = "key-name";
        Long value = new Long(3l);
        span.setTag(key, value);
        Assert.assertEquals(value, span.getTags().get(key));
    }

    @Test
    public void testTagForBoolean() {
        String key = "key-name";
        boolean value = false;
        span.setTag(key, value);
        Assert.assertEquals(value, span.getTags().get(key));
    }

    @Test
    public void testTagForString() {
        String key = "key-name";
        String value = "value-value";
        span.setTag(key, value);
        Assert.assertEquals(value, span.getTags().get(key));
    }

    @Test
    public void testTagForEmptyConditons() {
        String key = "key-name";
        String stringValue = "value-value";
        Long longValue = new Long(3l);
        boolean boolValue = false;
        
        span.setTag(null, stringValue);
        span.setTag(null, longValue);
        span.setTag(null, boolValue);
        span.setTag(key, (Number) null);
        span.setTag(key, (String) null);

        Assert.assertEquals(0, span.getTags().size());
    }

    @Test
    public void testLogForString() {
        long timestamp = 1111l;
        String key = "key-name";
        String value = "value-value";

        span.log(timestamp, key, value);

        List<LogData> logs = span.getLogs();
        Assert.assertEquals(1, logs.size());
        LogData data = logs.get(0);
        Assert.assertEquals((Long) timestamp, data.getTimestamp());
        Assert.assertEquals(key, data.getMessage());
        Assert.assertEquals(value, data.getPayload());
    }
    
    @Test
    public void testLogForEmptyConditions() {
        long timestamp = 1111l;
        String key = null;
        String value = "value-value";

        span.log(timestamp, key, value);
        span.log(key, value);
        span.log(key);
        span.log(timestamp, key);

        span.log(timestamp, Collections.<String, Object>emptyMap());
        span.log(Collections.<String, Object>emptyMap());

        List<LogData> logs = span.getLogs();
        Assert.assertEquals(0, logs.size());
    }


    @Test
    public void testSetOperationNameAfterFinish() {
        span.finish();

        try {
            span.setOperationName("bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, span.getErrors().size());
    }

    @Test
    public void testSetTagAfterFinish() {
        span.finish();

        try {
            span.setTag("bar", "foo");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, span.getErrors().size());
    }

    @Test
    public void testAddLogAfterFinish() {
        span.finish();

        try {
            span.log("bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, span.getErrors().size());
    }

    @Test
    public void testAddBaggageAfterFinish() {
        span.finish();

        try {
            span.setBaggageItem("foo", "bar");
            Assert.fail();
        } catch (RuntimeException ex) {
        }
        Assert.assertEquals(1, span.getErrors().size());
    }
}
