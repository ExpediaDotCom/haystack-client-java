package com.expedia.www.haystack.client.metrics;

public class MicrometerCounter implements Counter {
    private final io.micrometer.core.instrument.Counter delegate;

    public MicrometerCounter(io.micrometer.core.instrument.Counter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void increment(double amount) {
        delegate.increment(amount);
    }

    @Override
    public void decrement(double amount) {
        increment(-1 * amount);
    }

    @Override
    public double count() {
        return delegate.count();
    }
}
