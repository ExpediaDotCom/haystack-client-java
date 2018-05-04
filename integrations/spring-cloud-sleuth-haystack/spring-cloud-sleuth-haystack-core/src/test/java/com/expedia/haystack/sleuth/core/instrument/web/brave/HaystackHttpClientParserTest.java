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

package com.expedia.haystack.sleuth.core.instrument.web.brave;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import brave.SpanCustomizer;
import brave.servlet.HttpServletAdapter;

public class HaystackHttpClientParserTest {

    @Test
    public void testSpanName() {
        HaystackHttpClientParser haystackHttpClientParser = new HaystackHttpClientParser(new ArrayList<>());

        String requestURI = "http://localhost:8080/hello";

        String spanName = haystackHttpClientParser.spanName(new HttpServletAdapter(), new MockHttpServletRequest("get", requestURI));

        assertThat(spanName).isEqualTo(requestURI);
    }

    @Test
    public void testErrorWithNoError() {
        HaystackHttpClientParser haystackHttpClientParser = new HaystackHttpClientParser(new ArrayList<>());

        CustomSpanCustomizer customizer = new CustomSpanCustomizer();
        haystackHttpClientParser.error(200, null, customizer);

        assertThat(customizer.tags.get("error")).isEqualTo("false");
    }

    @Test
    public void testErrorWithBadStatusCode() {
        HaystackHttpClientParser haystackHttpClientParser = new HaystackHttpClientParser(new ArrayList<>());

        String requestURI = "http://localhost:8080/hello";

        CustomSpanCustomizer customizer = new CustomSpanCustomizer();
        haystackHttpClientParser.error(400, null, customizer);

        assertThat(customizer.tags.get("error")).isEqualTo("true");
        assertThat(customizer.tags.get("errorMessage")).isEqualTo("400");
    }

    class CustomSpanCustomizer implements SpanCustomizer {
        private String testedName;
        private Map<String, String> tags = new HashMap<>();

        String getTestedName() {
            return testedName;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        @Override
        public SpanCustomizer name(String name) {
            this.testedName = name;
            return this;
        }

        @Override
        public SpanCustomizer tag(String key, String value) {
            tags.put(key, value);
            return this;
        }

        @Override
        public SpanCustomizer annotate(String value) {
            return null;
        }

        @Override
        public SpanCustomizer annotate(long timestamp, String value) {
            return null;
        }
    }
}
