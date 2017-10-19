package com.expedia.www.haystack.client.dispatchers;

import java.io.Closeable;
import java.io.Flushable;

import com.expedia.www.haystack.client.Span;

/**
 * A Dispatcher is where a Tracer sends it's finished spans to be collected at some location.
 */
public interface Dispatcher extends Closeable, Flushable {

    /**
     * All dispatchers should dispatch to somewhere
     *
     * @param span Span to dispatch to the registered sink
     */
    void dispatch(Span span);

}

