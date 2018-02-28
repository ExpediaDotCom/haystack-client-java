package com.expedia.www.haystack.client.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public interface Timer {
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Updates the statistics kept by the counter with the specified
     * amount in nanoseconds.
     *
     * @param amount Duration of a single event being measured by this timer.
     */
    default void record(long duration) {
        record(duration, TimeUnit.NANOSECONDS);
    }

    /**
     * Updates the statistics kept by the counter with the specified amount.
     *
     * @param amount Duration of a single event being measured by this timer.
     * @param unit   Time unit for the amount being recorded.
     */
    void record(long duration, TimeUnit unit);

    /**
     * @param unit The base unit of time to scale the total to.
     * @return The total time of recorded events.
     */
    double totalTime(TimeUnit unit);

    /**
     * @return The number of events that have been called on this timer.
     */
    long count();

    /**
     * Returns a new {@link Sample}.
     *
     * @return a new {@link Sample}
     * @see Sample
     */
    Sample start();

    /**
     * Maintains state on the clock's start position for a latency sample. Complete the timing
     * by calling {@link Sample#stop()}.
     */
    interface Sample extends AutoCloseable {

        @Override
        default void close() {
            stop();
        }
        
        /**
         * Records the duration of the operation
         *
         * @return The total duration of the sample in nanoseconds
         */
        long stop();
    }

    class Builder {
        private final String name;
        private final Collection<Tag> tags = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder tags(Collection<Tag> tags) {
            this.tags.addAll(tags);
            return this;
        }

        public Builder tag(Tag tag) {
            tags.add(tag);
            return this;
        }

        public Timer register(MetricsRegistry registry) {
            return registry.timer(name, tags);
        }


    }
}

