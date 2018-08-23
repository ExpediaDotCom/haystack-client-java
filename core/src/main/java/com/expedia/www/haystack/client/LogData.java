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
package com.expedia.www.haystack.client;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LogData {
    private final Long timestamp;
    private final Map<String, ?> fields;

    public LogData(Long timestamp, String event) {
        this(timestamp,
                new HashMap<String, Object>(1) {
                    {
                        put(event, null);
                    }
                });
    }

    public LogData(Long timestamp, Map<String, ?> fields) {
        this.timestamp = timestamp;
        if (fields != null) {
            this.fields = Collections.<String, Object>unmodifiableMap(fields);
        } else {
            this.fields = Collections.<String, Object>emptyMap();
        }
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, fields);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LogData logData = (LogData) obj;
        return Objects.equals(timestamp, logData.getTimestamp())
                && Objects.equals(fields, logData.getFields());
    }

    /**
     * @return the timestamp
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the fields
     */
    public Map<String, ?> getFields() {
        return fields;
    }
}
