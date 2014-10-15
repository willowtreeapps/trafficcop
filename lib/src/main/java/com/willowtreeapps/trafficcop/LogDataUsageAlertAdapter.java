package com.willowtreeapps.trafficcop;

import android.util.Log;

/**
 * Created by evantatarka on 10/7/14.
 */
public class LogDataUsageAlertAdapter implements DataUsageAlertAdapter {
    private static final String TAG = "DataUsageWarning";

    private String tag;

    public LogDataUsageAlertAdapter() {
        this.tag = TAG;
    }

    public LogDataUsageAlertAdapter(String tag) {
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
