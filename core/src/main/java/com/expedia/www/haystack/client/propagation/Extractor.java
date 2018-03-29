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

import com.expedia.www.haystack.client.SpanContext;

/**
 * Interface for extracting a <code>SpanContext</code> from a carrier.
 *
 */
public interface Extractor<T> {


    /**
     * Extract a span's context from a carrier.
     *
     * @param carrier A carrier object that contains the required
     * context and baggage for creating a <code>SpanContext</code>.
     * @return The newly created <code>SpanContext</code>
     */
    SpanContext extract(T carrier);
}
