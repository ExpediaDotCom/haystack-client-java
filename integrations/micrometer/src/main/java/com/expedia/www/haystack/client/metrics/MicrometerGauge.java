package com.expedia.www.haystack.client.metrics;

public class MicrometerGauge<T> implements Gauge {
    private final io.micrometer.core.instrument.Gauge delegate;

    public MicrometerGauge(io.micrometer.core.instrument.Gauge delegate) {
        this.delegate = delegate;
    }

    @Override
    public double value() {
        return delegate.value();
    }
}
