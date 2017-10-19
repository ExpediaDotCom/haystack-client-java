package com.expedia.www.haystack.client.dispatchers.clients;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.formats.Format;

public class LoggerClient implements Client {
    private Format format;
    private Logger logger;

    public LoggerClient(Format format) {
        this(format, null);
    }

    public LoggerClient(Format format, Logger logger) {
        this.format = format;
        if (logger == null) {
            this. logger = LoggerFactory.getLogger(this.getClass());
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
}
