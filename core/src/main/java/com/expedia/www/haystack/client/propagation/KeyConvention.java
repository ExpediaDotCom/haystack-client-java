/*
 * Copyright 2018 Expedia, Inc.
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 *
 */
package com.expedia.www.haystack.client.propagation;

import java.util.Collection;

/**
 * Encapsulates the various key names and prefixes used to propagate a
 * trace context.
 *
 */
public interface KeyConvention {


    /**
     * Provides the prefix to use for baggage items in a carrier.
     *
     * @return The prefix for baggage items.
     */
    String baggagePrefix();

    /**
     * Provides the key used to propagate the trace-id
     *
     * @return The key used to propagate Trace-Id
     */
    String traceIdKey();

    /**
     * Provides the key names used to describe the trace-id in a
     * carrier.  Should include the value returned by
     * <code>traceIdKey()</code>.
     *
     * @return The collection of keys used for trace-id
     */
    Collection<String> traceIdKeyAliases();

    /**
     * Provides the key used to propagate the span-id
     *
     * @return The key used to propagate Span-Id
     */
    String spanIdKey();

    /**
     * Provides the key names used to describe the span-id in a carrier.
     * The list should include the value returned by
     * <code>spanIdKey()</code>.
     *
     * @return The collection of keys used for span-id
     */
    Collection<String> spanIdKeyAliases();

    /**
     * Provides the key used to propagate the parent-id
     *
     * @return The key used to propagate Parent-id
     */
    String parentIdKey();

    /**
     * Provides the key names used to describe the parent-id in the a
     * carrier.  This list should include the value returned by
     * <code>parentIdKey()</code>.
     *
     * @return The collection of keys used for parent-id
     */
    Collection<String> parentIdKeyAliases();
}
