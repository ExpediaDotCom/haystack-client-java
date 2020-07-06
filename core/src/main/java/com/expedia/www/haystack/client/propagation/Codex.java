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

/**
 * Interface to capture any formating required for propagation to work
 * with a carrier.
 *
 */
public interface Codex<R,T> {

    /**
     * Encode a value to the proper format needed by the carrier it's
     * paired with.
     *
     * @param value Value to encode in the proper format for the carrier
     * @return The encoded value
     */
    R encode(T value);

    /**
     * Decode a value from a carrier back into the original format.
     *
     * @param value The encoded value
     * @return The decoded value
     */
    T decode(R value);

    default T decodeTraceId(R value) {
        return decode(value);
    }

    default T decodeSpanId(R value) {
        return decode(value);
    }

    default T decodeBaggage(R value) {
        return decode(value);
    }

    default T decodeKey(R value) {
        return decode(value);
    }
}
