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
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.dispatchers.NoopDispatcher;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import io.opentracing.tag.StringTag;
import io.opentracing.tag.Tag;

public class SpanTest {

  private Tracer tracer;
  private Span span;
  private Dispatcher dispatcher;
  private MetricsRegistry metrics;

  @Before
  public void setUp() {
    metrics = new NoopMetricsRegistry();
    dispatcher = new NoopDispatcher();
    tracer = new Tracer.Builder(metrics, "TestService", dispatcher).build();
    span = tracer.buildSpan("TestOperation").start();
  }

  @Test
  public void testDuration() {
    String expected = "op-name";
    Span span = tracer.buildSpan(expected).withStartTimestamp(1L).start();

    span.finish(2L);

    Assert.assertEquals((Long) 1L, span.getStartTime());
    Assert.assertEquals((Long) 2L, span.getEndTime());
    Assert.assertEquals((Long) 1L, span.getDuration());
  }

  @Test
  public void testOperationName() {
    String expected = "op-name";
    String modString = "other-op-name";
    Span span = tracer.buildSpan(expected).start();
    Assert.assertEquals(expected, span.getOperationName());
    span.setOperationName(modString);
    Assert.assertEquals(modString, span.getOperationName());
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
    Long value = 3L;
    span.setTag(key, value);
    Assert.assertEquals(value, span.getTags().get(key));
  }

  @Test
  public void testTagForBoolean() {
    String key = "key-name";
    final boolean value = false;
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
  public void testTagForTagType() {
    String key = "typed-key-name";
    String value = "typed-tag-value-value";
    StringTag stringTag = new StringTag(key);
    stringTag.set(span, value);
    Assert.assertEquals(value, span.getTags().get(key));
  }

  @Test
  public void testTagForEmptyConditons() {
    String key = "key-name";
    String stringValue = "value-value";
    Long longValue = 3L;
    final boolean boolValue = false;

    span.setTag((Tag<String>) null, stringValue);
    span.setTag((Tag<Long>) null, longValue);
    span.setTag(null, boolValue);
    span.setTag(key, (Number) null);
    span.setTag(key, (String) null);

    Assert.assertEquals(0, span.getTags().size());
  }

  @Test
  public void testLogForEmptyConditions() {
    long timestamp = 1111L;
    final String key = null;

    span.log(key);
    span.log(timestamp, key);

    span.log(timestamp, Collections.emptyMap());
    span.log(Collections.emptyMap());

    List<LogData> logs = span.getLogs();
    Assert.assertEquals(0, logs.size());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetOperationNameAfterFinish() {
    span.finish();
    span.setOperationName("bar");
  }

  @Test(expected = IllegalStateException.class)
  public void testSetTagAfterFinish() {
    span.finish();
    span.setTag("bar", "foo");
  }

  @Test(expected = IllegalStateException.class)
  public void testAddLogAfterFinish() {
    span.finish();
    span.log("bar");
  }

  @Test(expected = IllegalStateException.class)
  public void testAddBaggageAfterFinish() {
    span.finish();
    span.setBaggageItem("foo", "bar");
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
    } catch (IllegalStateException ex) {
      //an InvalidStateException is expected here
    }

    try {
      dispatcher.flush();
    } catch (IOException ex) {
      Assert.fail();
    }

    Assert.assertEquals(0, dispatcher.getReportedSpans().size());
    Assert.assertEquals(1, dispatcher.getFlushedSpans().size());
    Assert.assertEquals(1, dispatcher.getReceivedSpans().size());
  }
}
