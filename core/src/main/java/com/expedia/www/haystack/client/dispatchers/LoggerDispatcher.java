package com.expedia.www.haystack.client.dispatchers;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;
import com.expedia.www.haystack.client.metrics.Timer.Sample;

public class LoggerDispatcher implements Dispatcher {
    private final Logger logger;

    private final Timer dispatchTimer;
    private final Counter closeCounter;
    private final Counter flushCounter;

    public LoggerDispatcher(Metrics metrics, Logger logger) {
        if (logger == null) {
            this.logger = LoggerFactory.getLogger(this.getClass());
        } else {
            this.logger = logger;
        }

        this.dispatchTimer = Timer.builder("dispatch").register(metrics);
        this.closeCounter = Counter.builder("close").register(metrics);
        this.flushCounter = Counter.builder("flush").register(metrics);
    }

    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE)
            .setExcludeFieldNames("metrics")
            .toString();
    }

    @Override
    public void dispatch(Span span) {
        try (Sample timer = dispatchTimer.start()) {
            logger.info("{}", span);
        }
    }

    @Override
    public void flush() throws IOException {
        // do nothing
        flushCounter.increment();
    }

    @Override
    public void close() throws IOException {
        // do nothing
        closeCounter.increment();
    }

    public static final class Builder {
        private final Metrics metrics;
        private Logger logger;

        public Builder(MetricsRegistry registry) {
            this(new Metrics(registry, Dispatcher.class.getName(), Arrays.asList(new Tag("type", "logger"))));
        }

        public Builder(Metrics metrics) {
            this.metrics = metrics;
        }

        public Builder withLogger(String loggerName) {
            this.logger = LoggerFactory.getLogger(loggerName);
            return this;
        }

        public Builder withLogger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public LoggerDispatcher build() {
            return new LoggerDispatcher(metrics, logger);
        }
    }

}
