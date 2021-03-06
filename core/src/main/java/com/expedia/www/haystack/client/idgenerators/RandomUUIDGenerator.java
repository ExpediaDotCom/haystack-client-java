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

/**
 * Generates UUIDs as ids for Traces and Spans.
 * <p/>
 * Consider using the TimeBasedUUIDGenerator which is more performant.
 *
 * @see com.expedia.www.haystack.client.idgenerators.TimeBasedUUIDGenerator
 */
public class RandomUUIDGenerator implements IdGenerator {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
