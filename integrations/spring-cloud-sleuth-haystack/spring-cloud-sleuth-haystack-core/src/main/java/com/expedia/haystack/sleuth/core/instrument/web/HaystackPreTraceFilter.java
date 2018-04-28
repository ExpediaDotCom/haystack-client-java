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

import static com.expedia.haystack.sleuth.core.haystack.KeyFactory.convertToSleuth;

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
import java.util.UUID;

import org.springframework.cloud.sleuth.instrument.web.TraceWebServletAutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import com.expedia.haystack.sleuth.core.haystack.TraceKeys;

import brave.internal.HexCodec;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Order(TraceWebServletAutoConfiguration.TRACING_FILTER_ORDER - 1)
@Slf4j
public class HaystackPreTraceFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            throw new ServletException("Filter just supports HTTP requests");
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        MutableHttpServletRequest mutableHttpServletRequest = new MutableHttpServletRequest(httpServletRequest);

        if (httpServletRequest.getHeader(TraceKeys.TRACE_ID) != null) {
            mutableHttpServletRequest.putHeader(convertToSleuth(TraceKeys.TRACE_ID), convertUUIDToHex(httpServletRequest.getHeader(TraceKeys.TRACE_ID)));
        }

        if (httpServletRequest.getHeader(TraceKeys.PARENT_ID) != null) {
            mutableHttpServletRequest.putHeader(convertToSleuth(TraceKeys.PARENT_ID), convertUUIDToHex(httpServletRequest.getHeader(TraceKeys.PARENT_ID)));
        }

        if (httpServletRequest.getHeader(TraceKeys.PARENT_MESSAGE_ID) != null) {
            mutableHttpServletRequest.putHeader(convertToSleuth(TraceKeys.PARENT_MESSAGE_ID), convertUUIDToHex(httpServletRequest.getHeader(TraceKeys.PARENT_MESSAGE_ID)));
        }

        if (httpServletRequest.getHeader(TraceKeys.DEBUG_TRACE) != null) {
            mutableHttpServletRequest.putHeader(convertToSleuth(TraceKeys.DEBUG_TRACE), convertUUIDToHex(httpServletRequest.getHeader(TraceKeys.DEBUG_TRACE)));
        }

        chain.doFilter(mutableHttpServletRequest, servletResponse);
    }

    public String convertUUIDToHex(String id) {
        val uuid = UUID.fromString(id);
        val leastSignificantBits = uuid.getLeastSignificantBits();
        val mostSignificantBits = uuid.getMostSignificantBits();

        return HexCodec.toLowerHex(mostSignificantBits, leastSignificantBits);
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
