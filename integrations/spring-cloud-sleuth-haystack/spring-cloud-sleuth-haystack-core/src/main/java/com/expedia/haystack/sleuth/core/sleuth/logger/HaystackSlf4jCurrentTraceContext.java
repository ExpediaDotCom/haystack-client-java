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

import javax.annotation.Nullable;

import java.util.UUID;

import org.slf4j.MDC;

import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HaystackSlf4jCurrentTraceContext extends CurrentTraceContext {

    private final String LEGACY_EXPORTABLE_NAME = "X-Span-Export";
    private final String LEGACY_PARENT_ID_NAME = "X-B3-ParentSpanId";
    private final String LEGACY_TRACE_ID_NAME = "X-B3-TraceId";
    private final String LEGACY_SPAN_ID_NAME = "X-B3-SpanId";

    private final CurrentTraceContext delegate;


    public HaystackSlf4jCurrentTraceContext(CurrentTraceContext delegate) {
        this.delegate = delegate;
    }

    public static HaystackSlf4jCurrentTraceContext create() {
        return create(CurrentTraceContext.Default.inheritable());
    }

    public static HaystackSlf4jCurrentTraceContext create(CurrentTraceContext currentTraceContext) {
        return new HaystackSlf4jCurrentTraceContext(currentTraceContext);
    }

    private void replace(String key, @Nullable String value) {
        if (value != null) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }

    @Override
    public TraceContext get() {
        return delegate.get();
    }

    @Override
    public Scope newScope(TraceContext traceContext) {
        String previousTraceId = MDC.get("traceId");
        String previousParentId = MDC.get("parentId");
        String previousSpanId = MDC.get("spanId");
        String spanExportable = MDC.get("spanExportable");
        String legacyPreviousTraceId = MDC.get(LEGACY_TRACE_ID_NAME);
        String legacyPreviousParentId = MDC.get(LEGACY_PARENT_ID_NAME);
        String legacyPreviousSpanId = MDC.get(LEGACY_SPAN_ID_NAME);
        String legacySpanExportable = MDC.get(LEGACY_EXPORTABLE_NAME);

        if (traceContext != null) {
            String traceId = generateTraceId(traceContext);
            MDC.put("traceId", traceId);
            MDC.put(LEGACY_TRACE_ID_NAME, traceId);

            String parentId = (traceContext.parentId() != null) ? new UUID(0, traceContext.parentId()).toString(): null;

            replace("parentId", parentId);
            replace(LEGACY_PARENT_ID_NAME, parentId);

            String spanId = new UUID(0, traceContext.spanId()).toString();
            MDC.put("spanId", spanId);
            MDC.put(LEGACY_SPAN_ID_NAME, spanId);

            String sampled = traceContext.sampled().toString();
            MDC.put("spanExportable", sampled);
            MDC.put(LEGACY_EXPORTABLE_NAME, sampled);

            log("Starting scope for span: {}", traceContext);

            if (traceContext.parentId() != null) {
                if (log.isTraceEnabled()) {
                    log.trace("With parent: {}", traceContext.parentId());
                }
            }
        } else {
            MDC.remove("traceId");
            MDC.remove("parentId");
            MDC.remove("spanId");
            MDC.remove("spanExportable");
            MDC.remove(LEGACY_TRACE_ID_NAME);
            MDC.remove(LEGACY_PARENT_ID_NAME);
            MDC.remove(LEGACY_SPAN_ID_NAME);
            MDC.remove(LEGACY_EXPORTABLE_NAME);
        }

        Scope scope = this.delegate.newScope(traceContext);

        class ThreadContextCurrentTraceContextScope implements Scope {

            @Override
            public void close() {
                log("Closing scope for span: {}", traceContext);
                scope.close();
                replace("traceId", previousTraceId);
                replace("parentId", previousParentId);
                replace("spanId", previousSpanId);
                replace("spanExportable", spanExportable);
                replace(LEGACY_TRACE_ID_NAME, legacyPreviousTraceId);
                replace(LEGACY_PARENT_ID_NAME, legacyPreviousParentId);
                replace(LEGACY_SPAN_ID_NAME, legacyPreviousSpanId);
                replace(LEGACY_EXPORTABLE_NAME, legacySpanExportable);
            }
        }

        return new ThreadContextCurrentTraceContextScope();
    }

    private void log(String text, TraceContext traceContext) {
        if (traceContext == null) {
            return;
        }
        if (log.isTraceEnabled()) {
            log.trace(text, traceContext);
        }
    }

    private String generateTraceId(TraceContext traceContext) {
        return new UUID(traceContext.traceIdHigh(), traceContext.traceId()).toString();
    }

}
