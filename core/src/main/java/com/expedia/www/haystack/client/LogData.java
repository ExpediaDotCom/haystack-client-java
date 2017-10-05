package com.expedia.www.haystack.client;

import java.util.Collections;
import java.util.Map;

public class LogData {
    private final Long timestamp;
    private final Map<String, ?> fields;
    private final String message;
    private final Object payload;

    public LogData(Long timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
        this.payload = null;
        this.fields = null;
    }

    public LogData(Long timestamp, String message, Object payload) {
        this.timestamp = timestamp;
        this.message = message;
        this.payload = payload;
        this.fields = null;
    }

    public LogData(Long timestamp, Map<String, ?> fields) {
        this.timestamp = timestamp;
        this.fields = fields;
        this.message = null;
        this.payload = null;
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
      return Collections.unmodifiableMap(fields);
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
