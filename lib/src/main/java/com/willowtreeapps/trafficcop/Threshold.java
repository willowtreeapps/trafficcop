package com.willowtreeapps.trafficcop;

/**
 * Created by evantatarka on 10/7/14.
 */
public class Threshold {
    private static final Threshold NONE = new Threshold(-1, -1);

    public final long bytes;
    public final int seconds;

    private Threshold(long bytes, int seconds) {
        this.bytes = bytes;
        this.seconds = seconds;
    }

    public boolean hasReached(DataUsage usage) {
        return !(bytes == -1 && seconds == -1)
                && (seconds >= usage.seconds && bytes <= usage.bytes
                || usage.seconds >= seconds && usage.bytes * seconds >= bytes * usage.seconds);
    }

    public static Builder of(long size, SizeUnit unit) {
        return new Builder(unit.of(size));
    }

    public static Threshold none() {
        return NONE;
    }

    public static class Builder {
        private long size;

        private Builder(long size) {
            this.size = size;
        }

        public Threshold per(int time, TimeUnit unit) {
            return new Threshold(size, unit.of(time));
        }

        public Threshold per(TimeUnit unit) {
            return new Threshold(size, unit.of(1));
        }
    }
}
