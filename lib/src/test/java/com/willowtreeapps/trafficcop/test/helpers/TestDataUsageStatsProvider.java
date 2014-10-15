package com.willowtreeapps.trafficcop.test.helpers;

import com.willowtreeapps.trafficcop.DataUsageStatsProvider;
import com.willowtreeapps.trafficcop.SizeUnit;
import com.willowtreeapps.trafficcop.TimeUnit;

/**
 * Created by evantatarka on 10/8/14.
 */
public class TestDataUsageStatsProvider implements DataUsageStatsProvider {
    private long nanoTime = 0;
    private long bytesTransmitted = 0;
    private long bytesReceived = 0;

    @Override
    public long getNanoTime() {
        return nanoTime;
    }

    @Override
    public long getBytesTransmitted() {
        return bytesTransmitted;
    }

    @Override
    public long getBytesReceived() {
        return bytesReceived;
    }

    public void incrementTime(int amount, TimeUnit unit) {
        nanoTime += (unit.of(amount) * 1000000000);
    }

    public void incrementTransmitted(int amount, SizeUnit unit) {
        bytesTransmitted += unit.of(amount);
    }

    public void incrementReceived(int amount, SizeUnit unit) {
        bytesReceived += unit.of(amount);
    }
}
