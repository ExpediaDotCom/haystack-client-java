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

import java.util.Random;

/**
 * Generates random and unique B3 compatible hexchar ids.
 * Note that traceId will be in 128 bit while spanId and parentSpanId will be 64 bit.
 */
public class HexcharIdGenerator implements IdGenerator {
    private final Random random = new Random();

    @Override
    public String generateTraceId() {
        return String.format("%016X", random.nextLong()).concat(String.format("%016X", random.nextLong()));
    }

    @Override
    public String generateSpanId() {
        return String.format("%016X", next64BitId());
    }

    @Override
    public Object generate() {
        return null;
    }

    /**
     * Generates a new 64-bit id, taking care to dodge zero which can be confused with absent
     */
    private long next64BitId() {
        long nextId = random.nextLong();
        while (nextId == 0L) {
            nextId = random.nextLong();
        }
        return nextId;
    }
}
