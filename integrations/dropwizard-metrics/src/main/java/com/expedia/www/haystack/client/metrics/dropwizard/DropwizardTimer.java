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
package com.expedia.www.haystack.client.metrics.dropwizard;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.expedia.www.haystack.client.metrics.Timer;

public class DropwizardTimer implements Timer {
    private final com.codahale.metrics.Timer delegate;
    private final AtomicLong totalTime = new AtomicLong(0);

    public DropwizardTimer(com.codahale.metrics.Timer delegate) {
        this.delegate = delegate;
    }

    @Override
    public double totalTime(TimeUnit unit) {
        return unit.convert(totalTime.get(), TimeUnit.NANOSECONDS);
    }

    @Override
    public long count() {
        return delegate.getCount();
    }

    @Override
    public void record(long duration, TimeUnit unit) {
        delegate.update(duration, unit);
        totalTime.addAndGet(TimeUnit.NANOSECONDS.convert(duration, unit));
    }

    @Override
    public Sample start() {
        return new DropwizardTimerSample(delegate.time());
    }

    public static class DropwizardTimerSample implements Timer.Sample {
        private final com.codahale.metrics.Timer.Context delegate;

        public DropwizardTimerSample(com.codahale.metrics.Timer.Context delegate) {
            this.delegate = delegate;
        }

        @Override
        public long stop() {
            return delegate.stop();
        }
    }
}
