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

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.sleuth.SpanAdjuster;

import zipkin2.Span;

/**
 * Fix trace name when an exception occurs and we are loosing the span's name
 */
public class ExceptionSpanAdjuster implements SpanAdjuster {

    @Override
    public Span adjust(Span span) {
        if (StringUtils.isEmpty(span.name())) {
            if (span.tags().containsKey("mvc.controller.method")) {
                return span.toBuilder().name(span.tags().get("mvc.controller.method")).kind(Span.Kind.SERVER).build();
            }
        }

        return span;
    }
}
