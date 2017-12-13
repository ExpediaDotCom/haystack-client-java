package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.formats.Format;

public class LoggerClient implements Client {
    // class level logger for application events
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerClient.class);

    private final Format<?> format;

    /** Event logger for data {@link LoggerClient#send sent} to the client instance */
    private final Logger logger;

    public LoggerClient(Format<?> format) {
        this(format, null);
    }

    public LoggerClient(Format<?> format, Logger logger) {
        this.format = format;
        if (logger == null) {
            this.logger = LoggerFactory.getLogger(this.getClass());
        } else {
            this.logger = logger;
        }
    }

    @Override
    public boolean send(Span span) {
        logger.info("{}", format.format(span));
        return true;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    public static final class Builder {
        private final Format<?> format;
        private String loggerName;

        public Builder(Format<?> format) {
            this.format = format;
        }

        public Builder withLogger(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public LoggerClient build() {
            if (loggerName != null) {
                return new LoggerClient(format, LoggerFactory.getLogger(loggerName));
            } else {
                return new LoggerClient(format);
            }
        }
    }

}
