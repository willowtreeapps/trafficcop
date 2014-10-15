package com.willowtreeapps.trafficcop;

/**
 * Created by evantatarka on 10/8/14.
 */
public interface DataUsageStatsProvider {
    public long getNanoTime();
    public long getBytesTransmitted();
    public long getBytesReceived();
}
