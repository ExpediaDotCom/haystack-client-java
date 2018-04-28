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

package com.expedia.haystack.sleuth.core.haystack;

import java.util.HashMap;
import java.util.Map;

public final class KeyFactory {

    /**
     * 128 or 64-bit trace ID lower-hex encoded into 32 or 16 characters (required)
     */
    public static String SLEUTH_TRACE_ID = "X-B3-TraceId";

    /**
     * 64-bit span ID lower-hex encoded into 16 characters (required)
     */
    public static String SLEUTH_SPAN_ID = "X-B3-SpanId";

    /**
     * 64-bit parent span ID lower-hex encoded into 16 characters (absent on root span)
     */
    public static String SLEUTH_PARENT_SPAN_ID = "X-B3-ParentSpanId";

    /**
     * "1" implies sampled and is a request to override collection-tier sampling policy.
     */
    public static String SLEUTH_FLAGS_DEBUG = "X-B3-Flags";

    private static Map<String, String> KEYS_MAPPING = new HashMap<>();

    static {
        KEYS_MAPPING.put(TraceKeys.TRACE_ID, SLEUTH_TRACE_ID);
        KEYS_MAPPING.put(TraceKeys.PARENT_ID, SLEUTH_SPAN_ID);
        KEYS_MAPPING.put(TraceKeys.PARENT_MESSAGE_ID, SLEUTH_PARENT_SPAN_ID);
        KEYS_MAPPING.put(TraceKeys.DEBUG_TRACE, SLEUTH_FLAGS_DEBUG);
    }

    protected KeyFactory() {
        // Do nothing
    }

    public static String convertToSleuth(String haystackTraceKeys) {
        return KEYS_MAPPING.getOrDefault(haystackTraceKeys, haystackTraceKeys);
    }
}
