package com.willowtreeapps.trafficcop;

/**
 * Created by evantatarka on 10/7/14.
 */
public enum TimeUnit {
    SECOND(1), SECONDS(1),
    MINUTE(60), MINUTES(60),
    HOUR(60 * 60), HOURS(60 * 60),
    DAY(60 * 60 * 24), DAYS(60 * 60 * 24),
    WEEK(60 * 60 * 24 * 7), WEEKS(60 * 60 * 24 * 7);

    private int scale;

    TimeUnit(int scale) {
        this.scale = scale;
    }

    public int of(int value) {
        return value * scale;
    }
}
