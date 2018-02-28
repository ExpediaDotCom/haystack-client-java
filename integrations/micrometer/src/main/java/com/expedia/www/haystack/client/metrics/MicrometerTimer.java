package com.expedia.www.haystack.client.metrics;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;

public class MicrometerTimer implements Timer {
    private final io.micrometer.core.instrument.Timer delegate;
    private final MeterRegistry registry;

    public MicrometerTimer(MeterRegistry registry, io.micrometer.core.instrument.Timer delegate) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @Override
    public void record(long duration, TimeUnit unit) {
        delegate.record(duration, unit);
    }

    @Override
    public double totalTime(TimeUnit unit) {
        return delegate.totalTime(unit);
    }

    @Override
    public long count() {
        return delegate.count();
    }

    @Override
    public Sample start() {
        return new MicrometerTimerSample(delegate, io.micrometer.core.instrument.Timer.start(registry));
    }

    public static class MicrometerTimerSample implements Timer.Sample {
        private final io.micrometer.core.instrument.Timer timer;
        private final io.micrometer.core.instrument.Timer.Sample delegate;

        public MicrometerTimerSample(io.micrometer.core.instrument.Timer timer, io.micrometer.core.instrument.Timer.Sample delegate) {
            this.timer = timer;
            this.delegate = delegate;
        }

        @Override
        public long stop() {
            return delegate.stop(timer);
        }
    }
}
