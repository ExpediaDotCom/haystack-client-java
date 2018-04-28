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

package com.expedia.haystack.sleuth.core.sleuth.logger;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.cloud.sleuth.util.ArrayListSpanReporter;

import brave.Span;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;

public class HaystackSlf4jCurrentTraceContextTest {

    private ArrayListSpanReporter reporter = new ArrayListSpanReporter();
    private Tracing tracing = Tracing.newBuilder()
                             .currentTraceContext(CurrentTraceContext.Default.create())
                             .spanReporter(this.reporter)
                             .build();

    private Span span = this.tracing.tracer().nextSpan().name("span").start();
    private HaystackSlf4jCurrentTraceContext haystackSlf4jCurrentTraceContext = HaystackSlf4jCurrentTraceContext.create();

    @Before
    @After
    public void setup() {
        MDC.clear();
    }

    @Test
    public void shouldSetEntriesToMdcFromSpan() {
        CurrentTraceContext.Scope scope = this.haystackSlf4jCurrentTraceContext.newScope(this.span.context());

        assertThat(MDC.get("X-B3-TraceId")).isEqualTo(new UUID(0L, span.context().traceId()).toString());
        assertThat(MDC.get("traceId")).isEqualTo(new UUID(0L, span.context().traceId()).toString());

        scope.close();

        assertThat(MDC.get("X-B3-TraceId")).isNullOrEmpty();
        assertThat(MDC.get("traceId")).isNullOrEmpty();
    }

    @Test
    public void shouldRemoveEntriesFromMdcFromNullSpan() {
        MDC.put("X-B3-TraceId", "A");
        MDC.put("traceId", "A");

        CurrentTraceContext.Scope scope = this.haystackSlf4jCurrentTraceContext.newScope(null);

        assertThat(MDC.get("X-B3-TraceId")).isNullOrEmpty();
        assertThat(MDC.get("traceId")).isNullOrEmpty();

        scope.close();

        assertThat(MDC.get("X-B3-TraceId")).isEqualTo("A");
        assertThat(MDC.get("traceId")).isEqualTo("A");
    }

}
