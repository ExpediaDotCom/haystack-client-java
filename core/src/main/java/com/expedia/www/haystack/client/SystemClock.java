package com.expedia.www.haystack.client;

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
        return System.currentTimeMillis() * 1000;
    }

    @Override
    public long milliTime() {
        return System.currentTimeMillis();
    }
}
