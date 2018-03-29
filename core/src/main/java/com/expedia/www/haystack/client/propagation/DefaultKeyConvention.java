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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class DefaultKeyConvention implements KeyConvention {

    private static final String BAGGAGE_PREFIX = "Baggage-";

    private static final String TRACE_ID = "Trace-ID";

    private static final String SPAN_ID = "Span-ID";

    private static final String PARENT_ID = "Parent-ID";

    @Override
    public String baggagePrefix() {
        return BAGGAGE_PREFIX;
    }

    @Override
    public String parentIdKey() {
        return PARENT_ID;
    }

    @Override
    public Collection<String> parentIdKeyAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(PARENT_ID));
    }


    @Override
    public String traceIdKey() {
        return TRACE_ID;
    }

    @Override
    public Collection<String> traceIdKeyAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(TRACE_ID));
    }

    @Override
    public String spanIdKey() {
        return SPAN_ID;
    }

    @Override
    public Collection<String> spanIdKeyAliases() {
        return Collections.unmodifiableCollection(Arrays.asList(SPAN_ID));
    }
}
