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
package com.expedia.www.haystack.client.dispatchers.clients;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expedia.www.haystack.client.Span;
import com.expedia.www.haystack.client.dispatchers.formats.Format;
import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Metrics;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;
import com.expedia.www.haystack.client.metrics.Timer.Sample;

public class LoggerClient implements Client {
    private final Format<?> format;

    /** Event logger for data {@link LoggerClient#send sent} to the client instance */
    private final Logger logger;

    private final Timer sendTimer;
    private final Counter closeCounter;
    private final Counter flushCounter;

    public LoggerClient(Metrics metrics, Format<?> format, Logger logger) {
        this.format = format;
        if (logger == null) {
            this.logger = LoggerFactory.getLogger(this.getClass());
        } else {
            this.logger = logger;
        }

        this.sendTimer = Timer.builder("send").register(metrics);
        this.closeCounter = Counter.builder("close").register(metrics);
        this.flushCounter = Counter.builder("flush").register(metrics);
    }

    @Override
    public boolean send(Span span) {
        try (Sample timer = sendTimer.start()) {
            logger.info("{}", format.format(span));
            return true;
        }
    }

    @Override
    public void close() {
        closeCounter.increment();
    }

    @Override
    public void flush() {
        flushCounter.increment();
    }

    public static final class Builder {
        private final Metrics metrics;
        private final Format<?> format;
        private String loggerName;

        public Builder(MetricsRegistry registry, Format<?> format) {
            this(new Metrics(registry, Client.class.getName(), Arrays.asList(new Tag("type", "logger"))), format);
        }

        public Builder(Metrics metrics, Format<?> format) {
            this.metrics = metrics;
            this.format = format;
        }

        public Builder withLogger(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public LoggerClient build() {
            if (loggerName != null) {
                return new LoggerClient(metrics, format, LoggerFactory.getLogger(loggerName));
            } else {
                return new LoggerClient(metrics, format, null);
            }
        }
    }

}
