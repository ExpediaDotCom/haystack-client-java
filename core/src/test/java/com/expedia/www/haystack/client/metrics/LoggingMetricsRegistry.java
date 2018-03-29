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
package com.expedia.www.haystack.client.metrics;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMetricsRegistry extends NoopMetricsRegistry implements MetricsRegistry {

    private final Logger logger;

    public LoggingMetricsRegistry() {
        this((Logger) null);
    }

    public LoggingMetricsRegistry(String logger) {
        this(LoggerFactory.getLogger(logger));
    }

    protected LoggingMetricsRegistry(Logger logger) {
        if (logger == null) {
            this.logger = LoggerFactory.getLogger(this.getClass());
        } else {
            this.logger = logger;
        }
    }

    @Override
    public <T> Gauge gauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f) {
        return new LoggerGauge<T>(name, tags, obj, f);
    }

    @Override
    public Counter counter(String name, Collection<Tag> tags) {
        return new LoggerCounter(name, tags);
    }

    @Override
    public Timer timer(String name, Collection<Tag> tags) {
        return new LoggerTimer(name, tags);
    }

    private class Meter {
        protected String name;

        public Meter(String name, Collection<Tag> tags) {
            this.name = generateName(name, tags);
        }

        protected String generateName(String name, Collection<Tag> tags) {
            if (tags.size() > 0) {
                return String.format("%s.%s", name,
                                     tags.stream()
                                     .map((x) -> String.join("=", x.key(), x.value()))
                                     .collect(Collectors.joining(".")));
            } else {
                return name;
            }
        }
    }

    public class LoggerGauge<T> extends Meter implements Gauge {
        T obj;
        ToDoubleFunction<T> f;

        public LoggerGauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f) {
            super(name, tags);
            this.obj = obj;
            this.f = f;
        }

        @Override
        public double value() {
            double value = f.applyAsDouble(obj);
            logger.info(String.format("%s: %s", name, value));
            return value;
        }
    }

    public class LoggerCounter extends Meter implements Counter {
        private double count = 0;

        public LoggerCounter(String name, Collection<Tag> tags) {
            super(name, tags);
        }

        @Override
        public void increment(double amount) {
            count += amount;
            logger.info(String.format("%s: %s", name, count));
        }

        @Override
        public void decrement(double amount) {
            count -= amount;
            logger.info(String.format("%s: %s", name, count));
        }

        @Override
        public double count() {
            logger.info(String.format("%s: %s", name, count));
            return count;
        }
    }

    public class LoggerTimer extends Meter implements Timer {
        private long count = 0;
        private long totalDuration = 0;


        public LoggerTimer(String name, Collection<Tag> tags) {
            super(name, tags);
        }

        @Override
        public void record(long duration, TimeUnit unit) {
            count++;
            totalDuration += unit.toMillis(duration);
            logger.info(String.format("%s.duration: %s %s", name, duration, unit));
        }

        @Override
        public double totalTime(TimeUnit unit) {
            long time = unit.convert(totalDuration, TimeUnit.MILLISECONDS);
            logger.info(String.format("%s.total: %s %s", name, time, unit));
            return time;
        }

        @Override
        public long count() {
            logger.info(String.format("%s.count: %s", name, count));
            return count;
        }

        @Override
        public Sample start() {
            return new LoggerSample(this, System.currentTimeMillis());
        }

        public class LoggerSample implements Timer.Sample {
            private Timer timer;
            private long time;

            public LoggerSample(Timer timer, long time) {
                this.timer = timer;
                this.time = time;
            }

            @Override
            public long stop() {
                long duration = System.currentTimeMillis() - time;
                timer.record(duration);
                return duration;
            }
        }
    }

}
