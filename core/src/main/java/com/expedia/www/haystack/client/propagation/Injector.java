package com.expedia.www.haystack.client.propagation;

import com.expedia.www.haystack.client.SpanContext;

/**
 * Interface for injecting <code>SpanContext</code> into a carrier.
 *
 */
public interface Injector<T> {


    /**
     * Inject the span's context into a carrier for propogation.
     *
     * @param context The <code>SpanContext</code> to propogate
     * @param carrier A carrier object that contains will pass along the required
     * context and baggage for creating a <code>SpanContext</code>.
     */
    void inject(SpanContext context, T carrier);
}
