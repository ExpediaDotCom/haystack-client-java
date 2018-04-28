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

import static org.assertj.core.api.Assertions.assertThat;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.expedia.haystack.sleuth.core.haystack.KeyFactory;
import com.expedia.haystack.sleuth.core.haystack.TraceKeys;

import brave.internal.HexCodec;

public class HaystackPreTraceFilterTest {

    @Test
    public void testWithoutHaystackHeaders() throws IOException, ServletException {
        String customHeader = "CUSTOM_HEADER";

        HaystackPreTraceFilter haystackPreTraceFilter = new HaystackPreTraceFilter();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader(customHeader, "myHeader");

        MockFilterChain chain = new MockFilterChain();

        haystackPreTraceFilter.doFilter(servletRequest, new MockHttpServletResponse(), chain);

        HaystackPreTraceFilter.MutableHttpServletRequest request = (HaystackPreTraceFilter.MutableHttpServletRequest) chain.getRequest();

        assertThat(request).isNotNull();
        assertThat(request.getHeader(customHeader)).isNotNull();
    }

    @Test
    public void testWithHaystackHeaders() throws IOException, ServletException {
        HaystackPreTraceFilter haystackPreTraceFilter = new HaystackPreTraceFilter();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        String traceId = new UUID(0, HexCodec.lowerHexToUnsignedLong("1339107135896364746")).toString();

        servletRequest.addHeader(TraceKeys.TRACE_ID, traceId);

        MockFilterChain chain = new MockFilterChain();

        haystackPreTraceFilter.doFilter(servletRequest, new MockHttpServletResponse(), chain);

        HaystackPreTraceFilter.MutableHttpServletRequest request = (HaystackPreTraceFilter.MutableHttpServletRequest) chain.getRequest();

        assertThat(request).isNotNull();
        String sleuthTraceKeys = KeyFactory.convertToSleuth(TraceKeys.TRACE_ID);
        String currentTraceId = request.getHeader(sleuthTraceKeys);
        assertThat(currentTraceId).isNotNull();
        assertThat(currentTraceId).isEqualTo(haystackPreTraceFilter.convertUUIDToHex(traceId));
    }
}
