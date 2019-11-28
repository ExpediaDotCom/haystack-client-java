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

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

import io.opentracing.tag.Tag;

public final class Span implements io.opentracing.Span {
  private final Tracer tracer;
  private final Clock clock;
  private final Map<String, Object> tags;
  private final List<LogData> logs;
  private final List<Reference> references;
  private SpanContext context;
  private String operationName;
  private final Long startTime;
  private Long duration;
  private Long endTime;
  private Boolean finished;

  Span(Tracer tracer, Clock clock, String operationName, SpanContext context, long startTime, Map<String, Object> tags, List<Reference> references) {
    this.tracer = tracer;
    this.clock = clock;
    this.operationName = operationName;
    this.context = context;
    this.startTime = startTime;
    this.tags = tags;
    finished = false;

    if (references == null) {
      this.references = Collections.emptyList();
    } else {
      this.references = Collections.unmodifiableList(references);
    }

    this.logs = new ArrayList<>();


    for (Map.Entry<String, Object> entry : tags.entrySet()) {
      this.tags.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public String toString() {
    return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
        .setExcludeFieldNames("tracer", "clock")
        .toString();
  }


  @Override
  public void finish() {
    finishTrace(clock.microTime());
  }

  @Override
  public void finish(long finishMicros) {
    synchronized (this) {
      finishTrace(finishMicros);
    }
  }

  /**
   * @return the duration
   */
  public Long getDuration() {
    synchronized (this) {
      return duration;
    }
  }

  /**
   * @return the endTime
   */
  Long getEndTime() {
    synchronized (this) {
      return endTime;
    }
  }

  /**
   * @return the endTime
   */
  public Long getStartTime() {
      return startTime;
  }

  /**
   * @return the tracer
   */
  public Tracer getTracer() {
    return this.tracer;
  }

  @Override
  public SpanContext context() {
    synchronized (this) {
      return this.context;
    }
  }

  public String getServiceName() {
    synchronized (this) {
      return getTracer().getServiceName();
    }
  }


  /**
   * @return the operatiomName
   */
  public String getOperationName() {
    synchronized (this) {
      return this.operationName;
    }
  }

  @Override
  public Span setOperationName(String operationName) {
    synchronized (this) {
      finishedCheck("Setting operation name (%s) to a finished span", operationName);
      this.operationName = operationName;
      return this;
    }
  }

  @Override
  public Span setBaggageItem(String key, String value) {
    synchronized (this) {
      if (key == null) {
        return this;
      }
      finishedCheck("Setting baggage (%s:%s) on a finished span", key, value);
      this.context = this.context.addBaggage(key, value);
      return this;
    }
  }

  @Override
  public String getBaggageItem(String key) {
    synchronized (this) {
      return this.context.getBaggageItem(key);
    }
  }

  public Map<String, String> getBaggageItems() {
    synchronized (this) {
      return context.getBaggage();
    }
  }

  @Override
  public Span setTag(String key, Number value) {
    return addTag(key, value);
  }

  @Override
  public <T> io.opentracing.Span setTag(Tag<T> tag, T value) {
    if (tag != null) {
      return addTag(tag.getKey(), value);
    }
    return this;
  }

  @Override
  public Span setTag(String key, boolean value) {
    return addTag(key, value);
  }

  @Override
  public Span setTag(String key, String value) {
    return addTag(key, value);
  }

  public Map<String, Object> getTags() {
    synchronized (this) {
      return Collections.unmodifiableMap(tags);
    }
  }

  @Override
  public Span log(long timestampMicroseconds, Map<String, ?> fields) {
    synchronized (this) {
      if (fields == null || fields.isEmpty()) {
        return this;
      }
      finishedCheck("Setting a log event (%s:%s) on a finished span", timestampMicroseconds, fields);
      logs.add(new LogData(timestampMicroseconds, fields));
      return this;
    }
  }

  @Override
  public Span log(long timestampMicroseconds, String event) {
    synchronized (this) {
      if (event == null) {
        return this;
      }
      finishedCheck("Setting a log event (%s:%s) on a finished span", timestampMicroseconds, event);
      logs.add(new LogData(timestampMicroseconds, event));
      return this;
    }
  }

  @Override
  public Span log(Map<String, ?> fields) {
    return log(System.nanoTime(), fields);
  }


  @Override
  public Span log(String event) {
    return log(System.nanoTime(), event);
  }

  public List<LogData> getLogs() {
    synchronized (this) {
      return Collections.unmodifiableList(logs);
    }
  }

  /**
   * Helper to record illegal access to span internals after <code>finish()</code>
   * has been called.
   *
   * @param format The string format to include in the execption message
   * @param args   Any arguments needed to populate the supplied format
   */
  private void finishedCheck(String format, Object... args) {
    if (finished) {
      throw new IllegalStateException(String.format(format, args));
    }
  }


  private Span addTag(String key, Object value) {
    synchronized (this) {
      if (key == null || value == null) {
        return this;
      }
      finishedCheck("Setting a tag (%s:%s) on a finished span", key, value);
      tags.put(key, value);
      return this;
    }
  }

  private void finishTrace(long finishMicros) {
    finishedCheck("Finishing a prior finished span");
    this.endTime = finishMicros;
    this.duration = endTime - startTime;
    finished = true;
    tracer.dispatch(this);
  }

  List<Reference> getReferences() {
    synchronized (this) {
      return references;
    }
  }


}
