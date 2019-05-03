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
import com.expedia.www.haystack.client.idgenerators.IdGenerator;
import com.expedia.www.haystack.client.idgenerators.LongIdGenerator;
import com.expedia.www.haystack.client.metrics.*;
import com.expedia.www.haystack.client.metrics.Timer;
import com.expedia.www.haystack.client.metrics.Timer.Sample;
import com.expedia.www.haystack.client.propagation.Extractor;
import com.expedia.www.haystack.client.propagation.Injector;
import com.expedia.www.haystack.client.propagation.PropagationRegistry;
import com.expedia.www.haystack.client.propagation.TextMapPropagator;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.IOException;
import java.util.*;

public class Tracer implements io.opentracing.Tracer {
    private final Dispatcher dispatcher;
    private final IdGenerator idGenerator;
    protected final Clock clock;
    protected final PropagationRegistry registry;
    private final String serviceName;
    private final ScopeManager scopeManager;
    private final boolean dualSpanMode;

    private final Counter spansCreatedCounter;

    private final Timer dispatchTimer;

    private final Timer closeTimer;
    private final Counter closeExceptionCounter;

    private final Timer flushTimer;
    private final Counter flushExceptionCounter;

    private final Timer injectTimer;
    private final Counter injectFailureCounter;

    private final Timer extractTimer;
    private final Counter extractFailureCounter;

    public Tracer(String serviceName, ScopeManager scopeManager, Clock clock,
                  IdGenerator idGenerator, Dispatcher dispatcher, PropagationRegistry registry, Metrics metrics) {
        this(serviceName, scopeManager, clock, idGenerator, dispatcher, registry, metrics, false);
    }
    public Tracer(String serviceName, ScopeManager scopeManager, Clock clock,
                  IdGenerator idGenerator, Dispatcher dispatcher, PropagationRegistry registry,
                  Metrics metrics, boolean dualSpanMode) {
        this.serviceName = serviceName;
        this.scopeManager = scopeManager;
        this.clock = clock;
        this.idGenerator = idGenerator;
        this.dispatcher = dispatcher;
        this.registry = registry;
        this.dualSpanMode = dualSpanMode;

        this.dispatchTimer = Timer.builder("dispatch").register(metrics);
        this.closeTimer = Timer.builder("close").register(metrics);
        this.closeExceptionCounter = Counter.builder("close").tag(new Tag("state", "exception")).register(metrics);
        this.flushTimer = Timer.builder("flush").register(metrics);
        this.flushExceptionCounter = Counter.builder("flush").tag(new Tag("state", "exception")).register(metrics);

        this.spansCreatedCounter = Counter.builder("spans").register(metrics);

        this.injectTimer = Timer.builder("inject").register(metrics);
        this.injectFailureCounter = Counter.builder("inject").tag(new Tag("state", "exception")).register(metrics);
        this.extractTimer = Timer.builder("extract").register(metrics);
        this.extractFailureCounter = Counter.builder("extract").tag(new Tag("state", "exception")).register(metrics);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Tracer.this.close();
            } catch (IOException e) {
                /* skip logging any error */
            }
        }));
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .setExcludeFieldNames("clock", "activeSource")
                .toString();
    }

    public void close() throws IOException {
        try (Sample timer = closeTimer.start()) {
            dispatcher.close();
        } catch (IOException e) {
            closeExceptionCounter.increment();
            throw e;
        }
    }

    public void flush() throws IOException {
        try (Sample timer = flushTimer.start()) {
            dispatcher.flush();
        } catch (IOException e) {
            flushExceptionCounter.increment();
            throw e;
        }
    }

    void dispatch(com.expedia.www.haystack.client.Span span) {
        try (Sample timer = dispatchTimer.start()) {
            dispatcher.dispatch(span);
        }
    }

    /**
     * @return the dispatcher
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * @return the serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        spansCreatedCounter.increment();
        return new SpanBuilder(this, clock, operationName);
    }


    @Override
    public <C> void inject(io.opentracing.SpanContext spanContext, Format<C> format, C carrier) {
        try (Sample sample = injectTimer.start()) {
            final Injector<C> injector = registry.getInjector(format);
            if (injector == null) {
                injectFailureCounter.increment();
                throw new IllegalArgumentException(String.format("Unsupported format: %s", format));
            } else if (!(spanContext instanceof SpanContext)) {
                injectFailureCounter.increment();
                throw new IllegalArgumentException(String.format("Invalid SpanContext type: %s", spanContext));
            }

            injector.inject((SpanContext) spanContext, carrier);
        }
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        try (Sample sample = extractTimer.start()) {
            final Extractor<C> extractor = registry.getExtractor(format);
            if (extractor == null) {
                extractFailureCounter.increment();
                throw new IllegalArgumentException(String.format("Unsupported format: %s", format));
            }
            return extractor.extract(carrier);
        }
    }

    @Override
    public Span activeSpan() {
        final Scope scope = scopeManager.active();
        return (scope == null ? null : scope.span());
    }

    @Override
    public ScopeManager scopeManager() {
        return scopeManager;
    }

    public static class SpanBuilder implements io.opentracing.Tracer.SpanBuilder {
        protected final Tracer tracer;
        protected Clock clock;
        protected Boolean ignoreActive;
        protected String operationName;
        protected Long startTime;

        protected final List<Reference> references;
        protected final Map<String, Object> tags;

        protected SpanBuilder(Tracer tracer, Clock clock, String operationName) {
            this.tracer = tracer;
            this.clock = clock;
            this.operationName = operationName;
            this.startTime = 0l;
            this.ignoreActive = false;
            this.references = new ArrayList<>();
            this.tags = new HashMap<>();
        }

        @Override
        public SpanBuilder asChildOf(io.opentracing.SpanContext parent) {
            return this.addReference(References.CHILD_OF, parent);
        }

        @Override
        public SpanBuilder asChildOf(Span parent) {
            if (parent == null) {
                return this;
            }
            return this.addReference(References.CHILD_OF, parent.context());
        }

        @Override
        public SpanBuilder addReference(String referenceType, io.opentracing.SpanContext referencedContext) {
            if (referencedContext == null) {
                return this;
            }

            if (!(referencedContext instanceof SpanContext)) {
                // can't do much here, so ignore it
                return this;
            }

            references.add(new Reference(referenceType, (SpanContext) referencedContext));
            return this;
        }

        @Override
        public SpanBuilder ignoreActiveSpan() {
            this.ignoreActive = true;
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, boolean value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, Number value) {
            this.tags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withStartTimestamp(long microseconds) {
            this.startTime = microseconds;
            return this;
        }

        boolean isServerSpan() {
            return Tags.SPAN_KIND_SERVER.equals(tags.get(Tags.SPAN_KIND.getKey()));
        }

        protected SpanContext createNewContext() {

            return createContext(tracer.idGenerator.generate(), tracer.idGenerator.generate(), null, Collections.emptyMap());
        }
        //remove
        protected SpanContext createContext(UUID traceId, UUID spanId, UUID parentId, Map<String, String> baggage) {
            return new SpanContext(traceId, spanId, parentId, baggage, false);
        }


        protected SpanContext createContext(Object traceId, Object spanId, Object parentId, Map<String, String> baggage) {//doubt parentId
            return new SpanContext(traceId, spanId, parentId, baggage, false);
        }

        protected SpanContext createDependentContext() {
            Reference parent = references.get(0);
            for (Reference reference : references) {
                if (References.CHILD_OF.equals(reference.getReferenceType())) {
                    // first parent wins
                    parent = reference;
                    break;
                }
            }

            Map<String, String> baggage = new HashMap<>();
            for (Reference reference : references) {
                baggage.putAll(reference.getContext().getBaggage());
            }

            // This is a check to see if the tracer is configured to support single
            // single span type (Zipkin style shared span id) or
            // dual span type (client and server having their own span ids ).
            // a. If tracer is not of dualSpanType and if it is a server span then we
            // just return the parent context with the same shared span ids
            // b. If tracer is not of dualSpanType and if the parent context is an extracted one from the wire
            // then we assume this is the first span in the server and so just return the parent context
            // with the same shared span ids
            if (!tracer.dualSpanMode && (isServerSpan() || parent.getContext().isExtractedContext())) {
                return createContext(parent.getContext().getTraceId(),
                                     parent.getContext().getSpanId(),
                                     parent.getContext().getParentId(),
                                     baggage);
            }

            return createContext(parent.getContext().getTraceId(),
                    UUID.randomUUID(),
                    parent.getContext().getSpanId(),
                    baggage);
        }

        SpanContext createContext() {
            // handle active spans if needed
            if (references.isEmpty() && !ignoreActive && tracer.activeSpan() != null) {
                asChildOf(tracer.activeSpan());
            }

            if (references.isEmpty()) {
                return createNewContext();
            }

            return createDependentContext();
        }

        private long calculateStartTime() {
            if (startTime == 0) {
                return clock.microTime();
            }
            return startTime;
        }

        @Override
        public Scope startActive(boolean finishSpanOnClose) {
            return tracer.scopeManager().activate(start(), finishSpanOnClose);
        }

        @Override
        @Deprecated
        public com.expedia.www.haystack.client.Span startManual() {
            return start();
        }

        @Override
        public com.expedia.www.haystack.client.Span start() {
            return new com.expedia.www.haystack.client.Span(tracer, clock, operationName, createContext(), calculateStartTime(), tags, references);
        }
    }


    public static class Builder {
        protected String serviceName;
        protected ScopeManager scopeManager = new ThreadLocalScopeManager();
        protected Clock clock = new SystemClock();
        protected Dispatcher dispatcher;
        protected PropagationRegistry registry = new PropagationRegistry();
        protected Metrics metrics;
        protected IdGenerator idGenerator;
        private boolean dualSpanMode;

        public Builder(MetricsRegistry registry, String serviceName, Dispatcher dispatcher) {
            this(new Metrics(registry, Tracer.class.getName(), Collections.emptyList()), serviceName, dispatcher);
        }

        public Builder(Metrics metrics, String serviceName, Dispatcher dispatcher) {
            this.serviceName = serviceName;
            this.dispatcher = dispatcher;
            this.metrics = metrics;

            TextMapPropagator textMapPropagator = new TextMapPropagator.Builder().build();
            withFormat(Format.Builtin.TEXT_MAP, (Injector<TextMap>) textMapPropagator);
            withFormat(Format.Builtin.TEXT_MAP, (Extractor<TextMap>) textMapPropagator);

            TextMapPropagator httpPropagator = new TextMapPropagator.Builder().withURLCodex().build();
            withFormat(Format.Builtin.HTTP_HEADERS, (Injector<TextMap>) httpPropagator);
            withFormat(Format.Builtin.HTTP_HEADERS, (Extractor<TextMap>) httpPropagator);
        }

        public Builder withScopeManager(ScopeManager scope) {
            this.scopeManager = scope;
            return this;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder withIdGenerator(IdGenerator idGenerator) {
            this.idGenerator = idGenerator;
            return this;
        }

        public <T> Builder withFormat(Format<T> format, Injector<T> injector) {
            registry.register(format, injector);
            return this;
        }

        public <T> Builder withFormat(Format<T> format, Extractor<T> extractor) {
            registry.register(format, extractor);
            return this;
        }

        public <T> Builder withoutFormat(Format<T> format) {
            registry.deregisterInjector(format);
            registry.deregisterExtractor(format);
            return this;
        }

        public Builder clearAllFormats() {
            registry.clear();
            return this;
        }

        /**
         * Enables production of client and server spans with two different span-ids. If not enabled,
         * this will cause the Tracer to produce client and server spans with shared span-ids (Zipkin style)
         * @return this builder instance
         */
        public Builder withDualSpanMode() {
            dualSpanMode = true;
            return this;
        }

        public Tracer build() {
            idGenerator = idGenerator == null ? new LongIdGenerator() : idGenerator;
            return new Tracer(serviceName, scopeManager, clock, idGenerator, dispatcher, registry, metrics, dualSpanMode);
        }
    }
}
