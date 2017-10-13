package com.expedia.www.haystack.client;

/**
 * Wrapper around common time calculations needed for creating various events within the system.
 */
public interface Clock {

    /**
     * Returns the current value of the registered timer, in
     * nanoseconds.  It's only as accurate as the underlying
     * implementation.
     *
     * @return Returns the current value of the registered timer, in nanoseconds.
     */
    long nanoTime();

    /**
     * Returns the current value of the registered timer, in
     * microseconds.  It's only as accurate as the underlying
     * implementation.
     *
     * @return Returns the current value of the registered timer, in microseconds.
     */
    long microTime();

    /**
     * Returns the current value of the registered timer, in
     * milliseconds.  It's only as accurate as the underlying
     * implementation.
     *
     * @return Returns the current value of the registered timer, in milliseconds.
     */
    long milliTime();
}
