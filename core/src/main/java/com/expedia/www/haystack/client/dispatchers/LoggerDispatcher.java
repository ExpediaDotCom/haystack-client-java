package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;

public class LoggerDispatcher implements Dispatcher {
    private final Logger logger;

    public LoggerDispatcher() {
        this(null);
    }

    public LoggerDispatcher(Logger logger) {
        if (logger == null) {
            this.logger = LoggerFactory.getLogger(this.getClass());
        } else {
            this.logger = logger;
        }
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
            .toString();
    }

    @Override
    public void dispatch(Span span) {
        logger.info("{}", span);
    }

    @Override
    public void flush() throws IOException {
        // do nothing
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

    public static final class Builder {
        private Logger logger;

        public Builder withLogger(String loggerName) {
            this.logger = LoggerFactory.getLogger(loggerName);
            return this;
        }

        public Builder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public LoggerDispatcher build() {
            return new LoggerDispatcher(logger);
        }
    }

}
