/*
 * Copyright 2019 Expedia, Inc.
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
package com.expedia.www.haystack.client.idgenerators;

public interface IdGenerator {

    /**
     * Generates a unique id
     * @deprecated This method was deprecated in 0.3.1. Use {@link #generateTraceId()}  and {@link #generateSpanId()} instead
     */
    @Deprecated
    Object generate();

    /**
     * Generates a random unique identifier for a trace.
     */
    default Object generateTraceId() {
        return generate();
    }

    /**
     * Generates a random identifier for a span. It should be unique within a trace.
     */
    default Object generateSpanId() {
        return generate();
    }
}
