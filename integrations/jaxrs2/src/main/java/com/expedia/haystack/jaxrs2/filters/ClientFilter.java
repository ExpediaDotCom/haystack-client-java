/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.haystack.jaxrs2.filters;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;

@Provider
public class ClientFilter implements ClientRequestFilter, ClientResponseFilter  {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFilter.class);

    /**
     * Name of the context parameter to hold the current span for the filter
     */
    public static final String CLIENT_SPAN_CONTEXT_KEY = ClientFilter.class.toString() + "-span";

    @Context
    protected ResourceInfo resourceInfo;

    private final Tracer tracer;

    public ClientFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        Tracer.SpanBuilder builder = tracer.buildSpan(getOperationName(requestContext, resourceInfo))
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
            .withTag(Tags.HTTP_URL.getKey(), requestContext.getUri().toString())
            .withTag(Tags.HTTP_METHOD.getKey(), requestContext.getMethod())
            .withTag(Tags.PEER_HOSTNAME.getKey(), requestContext.getUri().getHost())
            .withTag(Tags.PEER_PORT.getKey(), requestContext.getUri().getPort());

        final Span span = builder.startManual();

        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new ClientRequestContextTextMap(requestContext));
        requestContext.setProperty(CLIENT_SPAN_CONTEXT_KEY, span);
    }

    protected String getOperationName(ClientRequestContext context, ResourceInfo resourceInfo) {
        return String.format("%s:%s", context.getMethod(), resourceInfo.getResourceClass());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        try {
            Span span = (Span) requestContext.getProperty(CLIENT_SPAN_CONTEXT_KEY);
            if (span != null) {
                Tags.HTTP_STATUS.set(span, responseContext.getStatus());
                span.finish();
            }
        }
        catch (Exception e) {
            LOGGER.error("Client Tracing Filter failed:", e);
        }
    }
}
