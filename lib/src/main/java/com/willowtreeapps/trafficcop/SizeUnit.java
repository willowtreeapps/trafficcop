package com.willowtreeapps.trafficcop;

/**
 * Created by evantatarka on 10/7/14.
 */
public enum SizeUnit {
    BYTE(1), BYTES(1),
    KILOBYTE(1000), KILOBYTES(1000),
    MEGABYTE(1000 * 1000), MEGABYTES(1000 * 1000),
    GIGABYTE(1000 * 1000 * 1000), GIGABYTES(1000 * 1000 * 1000);

    private int scale;

    SizeUnit(int scale) {
        this.scale = scale;
    }

    public long of(long value) {
        return value * scale;
    }
}
