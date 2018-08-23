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
import java.util.Map.Entry;

public class SpanContext implements io.opentracing.SpanContext {

    private final Map<String, String> baggage;
    private final UUID traceId;
    private final UUID spanId;
    private final UUID parentId;

    public SpanContext(UUID traceId, UUID spanId, UUID parentId) {
        this(traceId, spanId, parentId, Collections.<String, String>emptyMap());
    }

    SpanContext(UUID traceId, UUID spanId, UUID parentId, Map<String, String> baggage) {
        if (baggage == null) {
            throw new NullPointerException();
        }

        this.traceId = traceId;
        this.spanId = spanId;
        this.parentId = parentId;
        this.baggage = Collections.unmodifiableMap(baggage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId, spanId, parentId, baggage);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SpanContext context = (SpanContext) obj;
        return Objects.equals(traceId, context.getTraceId())
                && Objects.equals(spanId, context.getSpanId())
                && Objects.equals(parentId, context.getParentId())
                && Objects.equals(baggage, context.getBaggage());
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .toString();
    }

    public SpanContext addBaggage(Map<String, String> newBaggage) {
        return new SpanContext(traceId, spanId, parentId, newBaggage);
    }

    public SpanContext addBaggage(String key, String value) {
        Map<String, String> newBaggage = new HashMap<>(this.baggage);
        newBaggage.put(key, value);
        return new SpanContext(traceId, spanId, parentId, newBaggage);
    }

    @Override
    public Iterable<Entry<String, String>> baggageItems() {
        return baggage.entrySet();
    }

    public Map<String, String> getBaggage() {
        return baggage;
    }

    public String getBaggageItem(String key) {
        return this.baggage.get(key);
    }


    /**
     * @return the traceId
     */
    public UUID getTraceId() {
        return traceId;
    }

    /**
     * @return the spanId
     */
    public UUID getSpanId() {
        return spanId;
    }

    /**
     * @return the parentId
     */
    public UUID getParentId() {
        return parentId;
    }
}
