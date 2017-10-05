package com.expedia.www.haystack.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
