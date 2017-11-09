package com.expedia.www.haystack.client.propagation;

import com.expedia.www.haystack.client.SpanContext;

/**
 * Interface for extracting a <code>SpanContext</code> from a carrier.
 *
 */
public interface Extractor<T> {


    /**
     * Extract a span's context from a carrier.
     *
     * @param carrier A carrier object that contains the required
     * context and baggage for creating a <code>SpanContext</code>.
     * @return The newly created <code>SpanContext</code>
     */
    SpanContext extract(T carrier);
}
