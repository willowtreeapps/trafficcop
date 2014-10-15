package com.willowtreeapps.trafficcop;

/**
 * A size unit with the base of bytes.
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

    /**
     * Converts the value from this unit to bytes
     *
     * @param value the size in this unit
     * @return the size in bytes
     */
    public long of(long value) {
        return value * scale;
    }
}
