package com.willowtreeapps.trafficcop;

/**
 * <p>
 * A threshold of data usage over a period of time. Data usage in a time period reaches this
 * threshold if either:</p>
 * <p>a) The time span of data usage is over the threshold and the average rate of usage is higher than the threshold.</p>
 * <p>b) The time span of data usage is under the threshold and the amount of data is over the threshold.</p>
 */
public class Threshold {
    private static final Threshold NONE = new Threshold(-1, -1);

    /**
     * The number of bytes that must be reached to hit the threshold.
     */
    public final long bytes;

    /**
     * The number of seconds that the bytes must be reached in.
     */
    public final int seconds;

    private Threshold(long bytes, int seconds) {
        this.bytes = bytes;
        this.seconds = seconds;
    }

    /**
     * Determines if the data usage has reached this threshold.
     *
     * @param usage the data usage to test
     * @return true if it reaches this threshold, false otherwise
     */
    public boolean hasReached(DataUsage usage) {
        return !(bytes == -1 && seconds == -1)
                && (seconds >= usage.seconds && bytes <= usage.bytes
                || usage.seconds >= seconds && usage.bytes * seconds >= bytes * usage.seconds);
    }

    /**
     * Constructs a Threshold that is reached when the given amount of data is used.
     *
     * @param size the size of data usage in the given unit
     * @param unit the unit the size is given in
     * @return a builder to chain the time span for the threshold
     */
    public static Builder of(long size, SizeUnit unit) {
        return new Builder(unit.of(size));
    }

    /**
     * Construct a Threshold that has no limit.
     *
     * @return the threshold
     */
    public static Threshold none() {
        return NONE;
    }

    public static class Builder {
        private long size;

        private Builder(long size) {
            this.size = size;
        }

        /**
         * Set the time span that this threshold must be reached in.
         *
         * @param time the time span in the given unit
         * @param unit the unit the time span is given in
         * @return the threshold
         */
        public Threshold per(int time, TimeUnit unit) {
            return new Threshold(size, unit.of(time));
        }

        /**
         * A convince method for {@link #per(int, TimeUnit)} for a time span of 1.
         *
         * @param unit the unit the time span is given in
         * @return the threshold
         */
        public Threshold per(TimeUnit unit) {
            return new Threshold(size, unit.of(1));
        }
    }
}
