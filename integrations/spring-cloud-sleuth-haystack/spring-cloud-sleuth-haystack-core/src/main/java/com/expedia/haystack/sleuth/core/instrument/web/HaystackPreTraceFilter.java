/*
 *  Copyright 2017 Expedia, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.expedia.haystack.sleuth.core.instrument.web;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.sleuth.instrument.web.TraceWebServletAutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import com.expedia.haystack.sleuth.core.haystack.B3Codex;
import com.expedia.www.haystack.client.propagation.KeyConvention;

import lombok.extern.slf4j.Slf4j;

@Order(TraceWebServletAutoConfiguration.TRACING_FILTER_ORDER - 1)
@Slf4j
public class HaystackPreTraceFilter extends GenericFilterBean {

    private final KeyConvention keyConvention;
    private final B3Codex codex;

    public HaystackPreTraceFilter(KeyConvention keyConvention, B3Codex codex) {
        this.keyConvention = keyConvention;
        this.codex = codex;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            throw new ServletException("Filter just supports HTTP requests");
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        MutableHttpServletRequest mutableHttpServletRequest = new MutableHttpServletRequest(httpServletRequest);

        keyConvention.traceIdKeyAliases().forEach(key -> {
            if (httpServletRequest.getHeader(key) != null) {
                mutableHttpServletRequest.putHeader(keyConvention.traceIdKey(), codex.decode(httpServletRequest.getHeader(key)));
            }
        });

        keyConvention.parentIdKeyAliases().forEach(key -> {
            if (httpServletRequest.getHeader(key) != null) {
                mutableHttpServletRequest.putHeader(keyConvention.parentIdKey(), codex.decode(httpServletRequest.getHeader(key)));
            }
        });

        keyConvention.spanIdKeyAliases().forEach(key -> {
            if (httpServletRequest.getHeader(key) != null) {
                mutableHttpServletRequest.putHeader(keyConvention.spanIdKey(), codex.decode(httpServletRequest.getHeader(key)));
            }
        });

        chain.doFilter(mutableHttpServletRequest, servletResponse);
    }

    class MutableHttpServletRequest extends HttpServletRequestWrapper {

        // holds custom header and value mapping
        private Map<String, String> customHeaders = new HashMap<>();

        MutableHttpServletRequest(HttpServletRequest request) {
            super(request);
        }

        void putHeader(String name, String value) {
            this.customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);

            if (headerValue == null) {
                return ((HttpServletRequest) getRequest()).getHeader(name);
            }

            return headerValue;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> set = new HashSet<>(customHeaders.keySet());

            Enumeration<String> headerNames = ((HttpServletRequest) getRequest()).getHeaderNames();

            while (headerNames.hasMoreElements()) {
                set.add(headerNames.nextElement());
            }

            return Collections.enumeration(set);
        }
    }
}
