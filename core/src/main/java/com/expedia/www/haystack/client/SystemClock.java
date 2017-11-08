package com.expedia.www.haystack.client;

import java.util.concurrent.TimeUnit;

/**
 * Clock implementation using the System timer for all calculations.
 *
 * @see System#nanoTime()
 * @see System#currentTimeMillis()
 */
public class SystemClock implements Clock {

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }

    @Override
    public long microTime() {
        return TimeUnit.MILLISECONDS.toMicros(milliTime());
    }

    @Override
    public long milliTime() {
        return System.currentTimeMillis();
    }
}
