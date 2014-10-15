package com.willowtreeapps.trafficcop;

/**
 * A listener that will be called when data usage reaches a threshold.
 */
public interface DataUsageAlertListener {
    /**
     * Called when data usage reaches a threshold.
     *
     * @param threshold the threshold reached
     * @param dataUsage the data used
     */
    void alertThreshold(Threshold threshold, DataUsage dataUsage);
}
