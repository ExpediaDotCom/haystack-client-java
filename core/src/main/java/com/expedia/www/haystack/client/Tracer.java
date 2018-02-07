package com.expedia.www.haystack.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.expedia.www.haystack.client.dispatchers.Dispatcher;
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
import io.opentracing.util.ThreadLocalScopeManager;

public class Tracer implements io.opentracing.Tracer {
    private final Dispatcher dispatcher;
    private final Clock clock;
    private final PropagationRegistry registry;
    private final String serviceName;
    private final ScopeManager scopeManager;

    public Tracer(String serviceName, ScopeManager scopeManager, Clock clock, Dispatcher dispatcher, PropagationRegistry registry) {
        this.serviceName = serviceName;
        this.scopeManager = scopeManager;
        this.clock = clock;
        this.dispatcher = dispatcher;
        this.registry = registry;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
            .setExcludeFieldNames("clock", "activeSource")
            .toString();
    }

    public void close() throws IOException {
        dispatcher.close();
    }

    public void flush() throws IOException {
        dispatcher.flush();
    }

    void dispatch(com.expedia.www.haystack.client.Span span) {
        dispatcher.dispatch(span);
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
        return new SpanBuilder(this, clock, operationName);
    }


    @Override
    public <C> void inject(io.opentracing.SpanContext spanContext, Format<C> format, C carrier) {
        final Injector<C> injector = registry.getInjector(format);
        if (injector == null) {
            throw new IllegalArgumentException(String.format("Unsupported format: %s", format));
        } else if (!(spanContext instanceof SpanContext)) {
            throw new IllegalArgumentException(String.format("Invalid SpanContext type: %s", spanContext));
        }

        injector.inject((SpanContext) spanContext, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        final Extractor<C> extractor = registry.getExtractor(format);
        if (extractor == null) {
            throw new IllegalArgumentException(String.format("Unsupported format: %s", format));
        }
        return extractor.extract(carrier);
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
        private final Tracer tracer;

        private Clock clock;
        private Boolean ignoreActive;
        private String operationName;
        private Long startTime;

        private final List<Reference> references;
        private final Map<String, Object> tags;

        public SpanBuilder(Tracer tracer, Clock clock, String operationName) {
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

        @Override
        public Scope startActive(boolean finishSpanOnClose) {
            return tracer.scopeManager().activate(start(), finishSpanOnClose);
        }

        private SpanContext createNewContext() {
            UUID randomId = UUID.randomUUID();
            UUID zero = new UUID(0l, 0l);
            return new SpanContext(randomId, randomId, zero);
        }

        private SpanContext createDependentContext() {
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

            return new SpanContext(parent.getContext().getTraceId(),
                                   UUID.randomUUID(),
                                   parent.getContext().getSpanId(),
                                   baggage);
        }

        private SpanContext createContext() {
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
        @Deprecated
        public com.expedia.www.haystack.client.Span startManual() {
            return start();
        }

        @Override
        public com.expedia.www.haystack.client.Span start() {
            return new com.expedia.www.haystack.client.Span(tracer, clock, operationName, createContext(), calculateStartTime(), tags, references);
        }
    }


    public static final class Builder {
        private String serviceName;
        private ScopeManager scopeManager = new ThreadLocalScopeManager();
        private Clock clock = new SystemClock();
        private Dispatcher dispatcher;
        private PropagationRegistry registry = new PropagationRegistry();

        public Builder(String serviceName, Dispatcher dispatcher) {
            this.serviceName = serviceName;
            this.dispatcher = dispatcher;

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

        public Tracer build() {
            return new Tracer(serviceName, scopeManager, clock, dispatcher, registry);
        }

    }
}
