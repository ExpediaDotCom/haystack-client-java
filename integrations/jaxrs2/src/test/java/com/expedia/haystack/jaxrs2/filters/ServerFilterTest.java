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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.expedia.haystack.annotations.DisableTracing;
import com.expedia.haystack.annotations.Traced;
import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.Tracer;
import com.expedia.www.haystack.client.dispatchers.InMemoryDispatcher;
import com.expedia.www.haystack.client.metrics.NoopMetricsRegistry;

import io.opentracing.tag.Tags;


@RunWith(MockitoJUnitRunner.class)
public class ServerFilterTest {

    private Tracer tracer;
    private InMemoryDispatcher dispatcher;

    @Mock
    private ResourceInfo resourceInfo;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @Mock
    private ContainerResponseContext containerResponseContext;


    @Before
    public void init() throws Exception {
        NoopMetricsRegistry metrics = new NoopMetricsRegistry();
        dispatcher = new InMemoryDispatcher.Builder(metrics).build();
        tracer = new Tracer.Builder(metrics, "server-filter-tests", dispatcher).build();
    }

    public static class Filter extends ServerFilter {
        private ResourceInfo resourceInfo;

        public Filter(Tracer tracer, ResourceInfo resourceInfo) {
            super(tracer);
            this.resourceInfo = resourceInfo;
        }

        @Override
        protected boolean shouldFilter(ContainerRequestContext context, ResourceInfo rInfo) {
            // use the mock
            return super.shouldFilter(context, resourceInfo);
        }

        @Override
        protected String getOperationName(ContainerRequestContext context, ResourceInfo rInfo) {
            // use the mock
            return super.getOperationName(context, resourceInfo);
        }
    }

    @Path("traced")
    public static class TracedResource {
        @Traced(name = "traced")
        @GET
        public String tracedMethodWithName() {
            return "hello";
        }

        public void tracedMethod() {
            // do nothing
        }
    }

    @Path("traced")
    @Traced(name = "annotated")
    public static class AnnotatedResource {
        @GET
        public void get() {
            // do nothing
        }
    }

    @Test
    public void testTracedResource() throws Exception {
        test_happy_path(TracedResource.class, "tracedMethod", "GET", "http://localhost/path", "GET:com.expedia.haystack.jaxrs2.filters.ServerFilterTest.TracedResource");
    }

    @Test
    public void testTracedAnnotatedResource() throws Exception {
        test_happy_path(AnnotatedResource.class, "get", "GET", "http://localhost/path", "annotated");
    }

    @Test
    public void testTracedMethod() throws Exception {
        test_happy_path(TracedResource.class, "tracedMethodWithName", "GET", "http://localhost/path", "traced");
    }


    private void test_happy_path(Class<?> resource, String methodName, String method, String url, String expectedOperationName) throws Exception {
        Mockito.<Class<?>>when(resourceInfo.getResourceClass()).thenReturn(resource);
        when(resourceInfo.getResourceMethod()).thenReturn(resource.getMethod(methodName));

        when(containerRequestContext.getMethod()).thenReturn(method);

        URI uri = new URI(url);
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePath()).thenReturn(uri);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
        when(containerRequestContext.getHeaders()).thenReturn(headers);

        ArgumentCaptor<Span> spanCaptor = ArgumentCaptor.forClass(Span.class);

        Filter filter = new Filter(tracer, resourceInfo);

        filter.filter(containerRequestContext);

        verify(containerRequestContext).setProperty(Matchers.any(), spanCaptor.capture());
        when(containerRequestContext.getProperty(Matchers.any())).thenReturn(spanCaptor.getValue());

        filter.filter(containerRequestContext, containerResponseContext);

        List<Span> spans = dispatcher.getReportedSpans();
        assertEquals(1, spans.size());

        Span span = spans.get(0);
        assertEquals(expectedOperationName, span.getOperatioName());

        Map<String, Object> tags = span.getTags();
        assertEquals(Tags.SPAN_KIND_SERVER, tags.get(Tags.SPAN_KIND.getKey()));
        assertEquals(method, tags.get(Tags.HTTP_METHOD.getKey()));
        assertEquals(uri.toString(), tags.get(Tags.HTTP_URL.getKey()));
    }

    @Path("ignored")
    @DisableTracing
    public static class DisabledResource {
        @GET
        public void get() {
            // do nothing
        }
    }

    @Path("ignoredmethod")
    public static class DisabledMethodResource {
        @GET
        @DisableTracing
        public void get() {
            // do nothing
        }
    }

    private void test_sad_path(Class<?> resource, String methodName, String method, String url) throws Exception {
        Mockito.<Class<?>>when(resourceInfo.getResourceClass()).thenReturn(resource);

        if (resource != null && methodName != null) {
            when(resourceInfo.getResourceMethod()).thenReturn(resource.getMethod(methodName));
        } else {
            when(resourceInfo.getResourceMethod()).thenReturn(null);
        }

        when(containerRequestContext.getMethod()).thenReturn(method);

        URI uri = new URI(url);
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getAbsolutePath()).thenReturn(uri);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();
        when(containerRequestContext.getHeaders()).thenReturn(headers);

        Filter filter = new Filter(tracer, resourceInfo);
        filter.filter(containerRequestContext);
        filter.filter(containerRequestContext, containerResponseContext);

        List<Span> spans = dispatcher.getReportedSpans();
        assertEquals(0, spans.size());
    }

    @Test
    public void testDisabledResoure() throws Exception {
        test_sad_path(DisabledResource.class, "get", "GET", "http://localhost/ignored");
    }

    @Test
    public void testDisabledResoureMethod() throws Exception {
        test_sad_path(DisabledResource.class, "get", "GET", "http://localhost/ignoredmethod");
    }

    @Test
    public void testMissingResourceClass() throws Exception {
        test_sad_path(null, null, "GET", "http://localhost/favicon.ico");
    }

    @Test
    public void testMissingResourceMethod() throws Exception {
        test_sad_path(TracedResource.class, null, "GET", "http://localhost/favicon.ico");
    }

}
