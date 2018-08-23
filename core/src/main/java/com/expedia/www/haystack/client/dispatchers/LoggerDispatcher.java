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
package com.expedia.www.haystack.client.dispatchers;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.metrics.*;
import com.expedia.www.haystack.client.metrics.Timer.Sample;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

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
        return new ReflectionToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
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
