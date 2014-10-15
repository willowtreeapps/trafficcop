package com.willowtreeapps.trafficcop;

import android.util.Log;

/**
 * A simple implementation of {@link DataUsageAlertListener} that just logs the alert as an error
 * when triggered.
 */
public class LogDataUsageAlertListener implements DataUsageAlertListener {
    private static final String TAG = "DataUsageWarning";

    private String tag;

    /**
     * Constructs a new listener that logs with the default tag "DataUsageWarning".
     */
    public LogDataUsageAlertListener() {
        this.tag = TAG;
    }

    /**
     * Constructs a new listener that logs with the given tag.
     *
     * @param tag the tag to log at.
     */
    public LogDataUsageAlertListener(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag cannot be null");
        }
        this.tag = tag;
    }

    @Override
    public void alertThreshold(Threshold threshold, DataUsage dataUsage) {
        Log.e(tag, dataUsage.getWarningMessage());
    }
}
