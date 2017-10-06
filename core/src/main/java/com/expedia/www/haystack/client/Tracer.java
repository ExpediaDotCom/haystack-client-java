package com.expedia.www.haystack.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.BaseSpan;
import io.opentracing.References;
import io.opentracing.propagation.Format;
import io.opentracing.util.ThreadLocalActiveSpanSource;

public class Tracer implements io.opentracing.Tracer {
    private final String serviceName;
    private final ActiveSpanSource activeSource;

    public Tracer(String serviceName, ActiveSpanSource activeSource) {
        this.serviceName = serviceName;
        this.activeSource = activeSource;
    }

	/**
	 * @return the serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public SpanBuilder buildSpan(String operationName) {
      return new SpanBuilder(this, operationName);
	}


	@Override
	public <C> void inject(io.opentracing.SpanContext spanContext, Format<C> format, C carrier) {
		// TODO Auto-generated method stub

	}

	@Override
	public <C> SpanContext extract(Format<C> format, C carrier) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public ActiveSpan activeSpan() {
        return activeSource.activeSpan();
    }

	@Override
	public ActiveSpan makeActive(io.opentracing.Span span) {
      return activeSource.makeActive(span);
	}


	public static class SpanBuilder implements io.opentracing.Tracer.SpanBuilder {

      private final Tracer tracer;
      

        private Boolean ignoreActive;
        private String operationName;
        private Long startTime;

        private final List<Reference> references;
        private final Map<String, Object> tags;

      public SpanBuilder(Tracer tracer, String operationName) {
          this.tracer = tracer;
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
		public SpanBuilder asChildOf(BaseSpan<?> parent) {
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
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, boolean value) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public SpanBuilder withTag(String key, Number value) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public SpanBuilder withStartTimestamp(long microseconds) {
        this.startTime = microseconds;
        return this;
		}

		@Override
		public ActiveSpan startActive() {
        return tracer.makeActive(startManual());
		}

      private SpanContext createNewContext() {
          UUID randomId = UUID.randomUUID();
          UUID zero = new UUID(0l, 0l);
          return new SpanContext(randomId, randomId, zero);
      }

      private SpanContext createDependantContext() {
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
          if (references.isEmpty()) {
              return createNewContext();
          }
          return createDependantContext();
      }

      private long calculateStartTime() {
          if (startTime == 0) {
              return System.nanoTime();
          }
          return startTime;
      }

		@Override
		public Span startManual() {
        return new Span(tracer, operationName, createContext(), calculateStartTime(), tags, references);
		}

        @Override
        public Span start() {
            return startManual();
        }
	}


    public static final class Builder {
        private String serviceName;
        private ActiveSpanSource activeSpanSource = new ThreadLocalActiveSpanSource();

        public Builder(String serviceName) {
            this.serviceName = serviceName;
        }

        public Builder withActiveSpanSource(ActiveSpanSource source) {
            this.activeSpanSource = source;
            return this;
        }

        public Tracer build() {
            return new Tracer(serviceName, activeSpanSource);
        }

    }
}