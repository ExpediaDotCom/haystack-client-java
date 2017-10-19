package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;

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
}
