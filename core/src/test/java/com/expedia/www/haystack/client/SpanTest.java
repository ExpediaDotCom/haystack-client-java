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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.expedia.www.haystack.client.dispatchers.Dispatcher;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;

public class SpanTest {

    private Tracer tracer;
    private Span span;
    private Dispatcher dispatcher;
    private MetricsRegistry metrics;

    @Before
    public void setUp() throws Exception {
        metrics = new NoopMetricsRegistry();
        dispatcher = new NoopDispatcher();
        tracer = new Tracer.Builder(metrics, "TestService", dispatcher).build();
        span = tracer.buildSpan("TestOperation").start();
    }

    @Test
    public void testDuration() {
        String expected = "op-name";
        Span span = tracer.buildSpan(expected).withStartTimestamp(1l).start();

        span.finish(2l);

        Assert.assertEquals((Long) 1l, span.getStartTime());
        Assert.assertEquals((Long) 2l, span.getEndTime());
        Assert.assertEquals((Long) 1l, span.getDuration());
    }

    @Test
    public void testOperationName() {
        String expected = "op-name";
        String modString = "other-op-name";
        Span span = tracer.buildSpan(expected).start();
        Assert.assertEquals(expected, span.getOperatioName());
        span.setOperationName(modString);
        Assert.assertEquals(modString, span.getOperatioName());
    }

    @Test
    public void testServiceName() {
        String expected = "service-name";
        Span span = new Tracer.Builder(metrics, expected, dispatcher).build().buildSpan(expected).start();
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
    public void testLogForEmptyConditions() {
        long timestamp = 1111l;
        String key = null;

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

    @Test
    public void testFinishAfterFinish() {
        InMemoryDispatcher dispatcher = new InMemoryDispatcher.Builder(metrics).build();
        tracer = new Tracer.Builder(metrics, "remote-dispatcher", dispatcher).build();

        Span span = tracer.buildSpan("operation").start();

        span.finish();

        try {
            span.finish();
            Assert.fail();
        } catch (RuntimeException ex) {
        }

        try {
            dispatcher.flush();
        } catch (IOException ex) {
            Assert.fail();
        }

        Assert.assertEquals(1, span.getErrors().size());
        Assert.assertEquals(0, dispatcher.getReportedSpans().size());
        Assert.assertEquals(1, dispatcher.getFlushedSpans().size());
        Assert.assertEquals(1, dispatcher.getReceivedSpans().size());
    }
}
