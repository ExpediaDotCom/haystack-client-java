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

import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Gauge;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Tag;
import com.expedia.www.haystack.client.metrics.Timer;

public class NoopMetricsRegistry implements MetricsRegistry {

    @Override
    public <T> Gauge gauge(String name, Collection<Tag> tags, T obj, ToDoubleFunction<T> f) {
        return new NoopGauge();
    }

    @Override
    public Timer timer(String name, Collection<Tag> tags) {
        return new NoopTimer();
    }

    @Override
    public Counter counter(String name, Collection<Tag> tags) {
        return new NoopCounter();
    }

    public static class NoopGauge implements Gauge {
        @Override
        public double value() {
            return -1;
        }
    }

    public static class NoopCounter implements Counter {
        @Override
        public void increment(double amount) {
        }

        @Override
        public void decrement(double amount) {
        }

        @Override
        public double count() {
            return -1;
        }
    }

    public static class NoopTimer implements Timer {
        @Override
        public void record(long duration, TimeUnit unit) {
        }

        @Override
        public double totalTime(TimeUnit unit) {
            return -1;
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public Sample start() {
            return new NoopSample();
        }

        public static class NoopSample implements Timer.Sample {
            @Override
            public long stop() {
                return 0;
            }
        }
    }
}
