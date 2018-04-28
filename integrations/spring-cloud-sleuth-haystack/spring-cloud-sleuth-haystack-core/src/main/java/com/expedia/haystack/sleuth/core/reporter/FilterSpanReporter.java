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

package com.expedia.haystack.sleuth.core.reporter;

import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@Slf4j
public class FilterSpanReporter implements Reporter<Span> {

    private final Reporter<Span> delegate;
    private final Pattern skipPattern;

    public FilterSpanReporter(Reporter<Span> delegate, Pattern skipPattern) {
        this.delegate = delegate;
        this.skipPattern = skipPattern;
    }

    @Override
    public void report(Span span) {
        if (span.name() == null) {
            log.error("Failed to find the name of the span. Please check and fix the problem.");
            return;
        }

        if (isExportable(span.name())) {
            delegate.report(span);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("The span ${span.name()} will not be sent to Haystack due to filtering");
            }
        }
    }

    private boolean isExportable(String name) {
        return !skipPattern.matcher(name).matches();
    }
}
