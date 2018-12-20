package com.expedia.haystack.opentracing.spring.starter.support;

import com.expedia.www.haystack.client.Tracer;

public interface TracerBuilderCustomizer {
    void customize(Tracer.Builder builder);
}
