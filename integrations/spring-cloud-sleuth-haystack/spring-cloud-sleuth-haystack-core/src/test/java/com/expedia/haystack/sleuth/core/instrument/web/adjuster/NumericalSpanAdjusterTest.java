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

package com.expedia.haystack.sleuth.core.instrument.web.adjuster;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.cloud.sleuth.util.ArrayListSpanReporter;

import brave.Span;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;

public class NumericalSpanAdjusterTest {

    @Test
    public void testWithoutTransformation() {
        NumericalSpanAdjuster numericalSpanAdjuster = new NumericalSpanAdjuster(new NumericalNameAdjuster());

        ArrayListSpanReporter reporter = new ArrayListSpanReporter();
        Tracing tracing = Tracing.newBuilder()
                                 .currentTraceContext(CurrentTraceContext.Default.create())
                                 .spanReporter(span -> {
                                     zipkin2.Span adjust = numericalSpanAdjuster.adjust(span);
                                     reporter.report(adjust);
                                 })
                                 .build();

        Span span = tracing.tracer().nextSpan().name("http://localhost:8080/hello").start();
        span.finish();

        assertThat(reporter.getSpans().size()).isEqualTo(1);
        assertThat(reporter.getSpans().get(0).name()).isEqualTo("http://localhost:8080/hello");
    }

    @Test
    public void testWithTransformation() {
        NumericalSpanAdjuster numericalSpanAdjuster = new NumericalSpanAdjuster(new NumericalNameAdjuster());

        ArrayListSpanReporter reporter = new ArrayListSpanReporter();
        Tracing tracing = Tracing.newBuilder()
                                 .currentTraceContext(CurrentTraceContext.Default.create())
                                 .spanReporter(span -> {
                                     zipkin2.Span adjust = numericalSpanAdjuster.adjust(span);
                                     reporter.report(adjust);
                                 })
                                 .build();

        Span span = tracing.tracer().nextSpan().name("http://localhost:8080/125478").start();
        span.finish();

        assertThat(reporter.getSpans().size()).isEqualTo(1);
        assertThat(reporter.getSpans().get(0).name()).isEqualTo("http://localhost:8080/_");
    }
}
