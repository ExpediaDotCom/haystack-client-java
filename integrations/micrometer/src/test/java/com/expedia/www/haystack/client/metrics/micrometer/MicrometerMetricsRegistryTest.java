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
package com.expedia.www.haystack.client.metrics.micrometer;

import static java.util.Collections.emptyList;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.expedia.www.haystack.client.metrics.Counter;
import com.expedia.www.haystack.client.metrics.Gauge;
import com.expedia.www.haystack.client.metrics.MetricsRegistry;
import com.expedia.www.haystack.client.metrics.Timer;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MicrometerMetricsRegistryTest {

    private final MetricsRegistry registry = new MicrometerMetricsRegistry(new SimpleMeterRegistry());

    @Test
    public void gaugeOnNullValue() {
        Gauge gauge = registry.gauge("gauge", emptyList(), null, obj -> 1.0);
        assertEquals(gauge.value(), Double.NaN, 0);
    }

    @Test
    public void metersOnNullTags() {
        Gauge gauge = registry.gauge("gauge", null, null, obj -> 1.0);
        assertEquals(gauge.value(), Double.NaN, 0);
        Counter counter = registry.counter("counter", null);
        assertEquals(counter.count(), 0, 0);
        Timer timer = registry.timer("timer", null);
        assertEquals(timer.count(), 0, 0);
    }
}
