package com.expedia.haystack.jaxrs2.feature;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.haystack.jaxrs2.filters.ClientFilter;
import com.expedia.haystack.jaxrs2.filters.ServerFilter;

import io.opentracing.Tracer;

@Provider
public class HaystackFeature implements Feature {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaystackFeature.class);

    private final Tracer tracer;

    public HaystackFeature(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean configure(FeatureContext context) {
        if (context.getConfiguration().isEnabled(this.getClass())) {
            return false;
        }

        switch (context.getConfiguration().getRuntimeType()) {
        case SERVER:
            context.register(new ServerFilter(tracer));
            break;
        case CLIENT:
            context.register(new ClientFilter(tracer));
            break;
        default:
            LOGGER.error("Unknown runtime ({}), not registering Haystack feature", context.getConfiguration().getRuntimeType());
            return false;
        }
        return true;
    }
}
