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


import java.util.UUID;

import static com.fasterxml.uuid.Generators.timeBasedGenerator;

/**
 * Generates UUIDs as ids for Traces and Spans.
 * <p/>
 * Given that the span only needs to be unique within a trace, the UUID for spans will only contain the least
 * significant bits and will be left padded with zeroes.
 */
public class TimeBasedUUIDGenerator implements IdGenerator {

    @Override
    public UUID generate() {
        return timeBasedGenerator().generate();
    }

    @Override
    public UUID generateSpanId() {
        return new UUID(0, timeBasedGenerator().generate().getLeastSignificantBits());
    }
}
