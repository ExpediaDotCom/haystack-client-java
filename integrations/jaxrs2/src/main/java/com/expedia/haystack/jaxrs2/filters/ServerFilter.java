package com.expedia.haystack.jaxrs2.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.haystack.annotations.DisableTracing;
import com.expedia.haystack.annotations.Traced;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

@Provider
public class ServerFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFilter.class);

    private final Tracer tracer;

    @Context
    protected ResourceInfo resourceInfo;

    /**
     * Name of the context parameter to hold the current span for the filter
     */
    private static final String SERVER_SPAN_CONTEXT_KEY = ServerFilter.class.toString() + "-span";

    public ServerFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        if (!shouldFilter(context, resourceInfo)) {
            // do nothing if the filter doesn't apply
            return;
        }

        try {
            Tracer.SpanBuilder builder = tracer.buildSpan(getOperationName(context, resourceInfo))
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                .withTag(Tags.HTTP_METHOD.getKey(), context.getMethod())
                .withTag(Tags.HTTP_URL.getKey(), context.getUriInfo().getAbsolutePath().toString());

            builder.asChildOf(tracer.extract(Format.Builtin.HTTP_HEADERS, new ContainerRequestContextTextMap(context)));
            final Span span = builder.startManual();

            context.setProperty(SERVER_SPAN_CONTEXT_KEY, span);
        } catch (Exception e) {
            LOGGER.error("Server Request Filter failed", e);
        }
    }

  /**
   * Helper to determine if the filter applies in the current context or not.
   *
   * @param context Context provided on the request
   * @param resourceInfo Resource Info injected via @Context
   * @return <code>true</code> if this filter should apply to the current context
   */
    protected boolean shouldFilter(ContainerRequestContext context, ResourceInfo resourceInfo) {
        if (resourceInfo.getResourceMethod().getAnnotation(DisableTracing.class) != null
            || resourceInfo.getResourceClass().getAnnotation(DisableTracing.class) != null) {
            return false;
        }
        return true;
    }

    /**
     * Extension point used to supply the operation name for tracing.
     *
     * @param context Context provided on the request
     * @param resourceInfo Resource Info injected via @Context
     * @return the name used for this operation
     */
    protected String getOperationName(ContainerRequestContext context, ResourceInfo resourceInfo) {
        final Traced methodAnnotation = resourceInfo.getResourceMethod().getAnnotation(Traced.class);
        if (methodAnnotation != null) {
            return methodAnnotation.name();
        }

        final Traced classAnnotation = resourceInfo.getResourceClass().getAnnotation(Traced.class);
        if (classAnnotation != null) {
            return classAnnotation.name();
        }

        return String.format("%s:%s", context.getMethod(), resourceInfo.getResourceClass().getCanonicalName());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (!shouldFilter(requestContext, resourceInfo)) {
            // do nothing if the filter doesn't apply
            return;
        }

        try {
            Span span = (Span) requestContext.getProperty(SERVER_SPAN_CONTEXT_KEY);
            if (span != null) {
                span.setTag(Tags.HTTP_STATUS.getKey(), responseContext.getStatus());
                span.finish();
            }
        } catch (Exception e) {
            LOGGER.error("Server Response Filter failed", e);
        }
    }
}
