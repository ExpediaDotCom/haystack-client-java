package com.expedia.haystack.jaxrs2.feature;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.expedia.haystack.jaxrs2.filters.ClientFilter;
import com.expedia.haystack.jaxrs2.filters.ServerFilter;

import io.opentracing.Tracer;

@Provider
public class HaystackFeature implements Feature {
    private final Tracer tracer;

    public HaystackFeature(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean configure(FeatureContext context) {
        if (context.getConfiguration().isEnabled(this.getClass())) {
            return false;
        }

        context.register(new ServerFilter(tracer));
        context.register(new ClientFilter(tracer));
        return true;
    }
}
