package com.expedia.www.haystack.client.propagation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DefaultKeyConvention implements KeyConvention {

    private static final String BAGGAGE_PREFIX = "Baggage-";

    private static final String TRACE_ID = "Trace-ID";

    private static final String SPAN_ID = "Span-ID";

    private static final String PARENT_ID = "Parent-ID";

    @Override
    public String baggagePrefix() {
        return BAGGAGE_PREFIX;
    }

    @Override
    public String parentIdKey() {
        return PARENT_ID;
    }

    @Override
    public Collection<String> parentIdKeyAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(PARENT_ID));
    }


    @Override
    public String traceIdKey() {
        return TRACE_ID;
    }

    @Override
    public Collection<String> traceIdKeyAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(TRACE_ID));
    }

    @Override
    public String spanIdKey() {
        return SPAN_ID;
    }

    @Override
    public Collection<String> spanIdKeyAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(SPAN_ID));
    }
}
