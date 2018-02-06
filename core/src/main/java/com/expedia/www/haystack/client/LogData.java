package com.expedia.www.haystack.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

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
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
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
