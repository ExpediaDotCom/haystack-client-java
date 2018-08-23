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
package com.expedia.www.haystack.client.propagation;

import com.expedia.www.haystack.client.SpanContext;
import io.opentracing.propagation.TextMap;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TextMapPropagator implements Injector<TextMap>, Extractor<TextMap> {

    private final KeyConvention convention;
    private final TextMapCodex keyCodex;
    private final TextMapCodex valueCodex;

    protected TextMapPropagator(KeyConvention convention, TextMapCodex keyCodex, TextMapCodex valueCodex) {
        this.convention = convention;
        this.keyCodex = keyCodex;
        this.valueCodex = valueCodex;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .toString();
    }

    private String prefixKey(String prefix, String key) {
        if (prefix == null || prefix.isEmpty()) {
            return key;
        }
        return String.format("%s%s", prefix, key);
    }

    private String unprefixKey(String prefix, String key) {
        if (prefix == null || prefix.isEmpty()) {
            return key;
        } else if (!key.startsWith(prefix)) {
            return key;
        }

        return key.substring(prefix.length());
    }


    private void put(TextMap carrier, Object key, Object value) {
        carrier.put(keyCodex.encode(key), valueCodex.encode(value));
    }

    @Override
    public void inject(SpanContext context, TextMap carrier) {
        put(carrier, convention.traceIdKey(), context.getTraceId());
        put(carrier, convention.spanIdKey(), context.getSpanId());
        put(carrier, convention.parentIdKey(), context.getParentId());

        for (Map.Entry<String, String> baggage : context.baggageItems()) {
            put(carrier, prefixKey(convention.baggagePrefix(), baggage.getKey()), baggage.getValue());
        }
    }

    @Override
    public SpanContext extract(TextMap carrier) {
        String traceId = null;
        String parentId = null;
        String spanId = null;

        final Map<String, String> baggage = new HashMap<>();

        for (Map.Entry<String, String> entry : carrier) {
            final String key = entry.getKey();
            if (keyCodex.decode(key).startsWith(convention.baggagePrefix())) {
                baggage.put(unprefixKey(convention.baggagePrefix(), keyCodex.decode(key)), valueCodex.decode(entry.getValue()));
            } else if (convention.traceIdKeyAliases().contains(keyCodex.decode(key))) {
                traceId = valueCodex.decode(entry.getValue());
            } else if (convention.parentIdKeyAliases().contains(keyCodex.decode(key))) {
                parentId = valueCodex.decode(entry.getValue());
            } else if (convention.spanIdKeyAliases().contains(keyCodex.decode(key))) {
                spanId = valueCodex.decode(entry.getValue());
            }
        }

        if (traceId == null || spanId == null) {
            // throw an IllegalArgumentException due to invalid propagation?
            return null;
        }

        SpanContext context = new SpanContext(UUID.fromString(traceId),
                UUID.fromString(spanId),
                parentId == null ? null : UUID.fromString(parentId));
        return context.addBaggage(baggage);
    }


    public static class Builder {
        private TextMapCodex keyCodex = new TextMapCodex();
        private TextMapCodex valueCodex = new TextMapCodex();
        private KeyConvention convention = new DefaultKeyConvention();

        public Builder withURLCodex() {
            this.keyCodex = new TextMapURLCodex();
            this.valueCodex = new TextMapURLCodex();
            return this;
        }

        public Builder withKeyConvention(KeyConvention convention) {
            this.convention = convention;
            return this;
        }

        public Builder withCodex(TextMapCodex codex) {
            this.keyCodex = codex;
            this.valueCodex = codex;
            return this;
        }

        public Builder withKeyCodex(TextMapCodex codex) {
            this.keyCodex = codex;
            return this;
        }

        public Builder withValueCodex(TextMapCodex codex) {
            this.valueCodex = codex;
            return this;
        }

        public TextMapPropagator build() {
            return new TextMapPropagator(convention, keyCodex, valueCodex);
        }

    }
}
