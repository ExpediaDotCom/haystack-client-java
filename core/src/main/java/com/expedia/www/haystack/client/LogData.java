package com.expedia.www.haystack.client;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class LogData {
    private final Long timestamp;
    private final Map<String, ?> fields;
    private final String message;
    private final Object payload;

    public LogData(Long timestamp, String message) {
        this(timestamp, message, null);
    }

    public LogData(Long timestamp, String message, Object payload) {
        this.timestamp = timestamp;
        this.message = message;
        this.payload = payload;
        this.fields = Collections.<String, Object>emptyMap();
    }

    public LogData(Long timestamp, Map<String, ?> fields) {
        this.timestamp = timestamp;
        if (fields != null) {
            this.fields = Collections.<String, Object>unmodifiableMap(fields);
        } else {
            this.fields = Collections.<String, Object>emptyMap();
        }

        this.message = null;
        this.payload = null;
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
            .toString();
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

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the payload
     */
    public Object getPayload() {
        return payload;
    }

}
