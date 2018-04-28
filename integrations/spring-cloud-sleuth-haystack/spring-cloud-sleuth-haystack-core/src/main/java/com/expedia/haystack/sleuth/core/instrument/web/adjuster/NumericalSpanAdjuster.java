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

import org.springframework.cloud.sleuth.SpanAdjuster;

import zipkin2.Span;

public class NumericalSpanAdjuster implements SpanAdjuster {

    private final NumericalNameAdjuster numericalNameAdjuster;

    public NumericalSpanAdjuster(NumericalNameAdjuster numericalNameAdjuster) {
        this.numericalNameAdjuster = numericalNameAdjuster;
    }

    @Override
    public Span adjust(Span span) {
        if (span.name() != null) {
            return span.toBuilder().name(numericalNameAdjuster.adjustName(span.name())).build();
        }

        return span;
    }
}
