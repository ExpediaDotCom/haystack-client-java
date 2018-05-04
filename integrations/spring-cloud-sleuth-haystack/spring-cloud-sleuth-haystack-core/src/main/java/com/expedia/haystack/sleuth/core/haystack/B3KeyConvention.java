package com.expedia.haystack.sleuth.core.haystack;

import com.expedia.www.haystack.client.propagation.DefaultKeyConvention;

public class B3KeyConvention extends DefaultKeyConvention {

    /**
     * 128 or 64-bit trace ID lower-hex encoded into 32 or 16 characters (required)
     */
    private final String TRACE_ID_NAME = "X-B3-TraceId";

    /**
     * 64-bit span ID lower-hex encoded into 16 characters (required)
     */
    private final String SPAN_ID_NAME = "X-B3-SpanId";

    /**
     * 64-bit parent span ID lower-hex encoded into 16 characters (absent on root span)
     */
    private final String PARENT_SPAN_ID_NAME = "X-B3-ParentSpanId";

    @Override
    public String traceIdKey() {
        return TRACE_ID_NAME;
    }

    @Override
    public String spanIdKey() {
        return SPAN_ID_NAME;
    }

    @Override
    public String parentIdKey() {
        return PARENT_SPAN_ID_NAME;
    }
}
