package com.willowtreeapps.trafficcop;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A simple way to get detect data usage over a threshold.
 */
public class TrafficCop {
    private static final String SHARED_PREFS_NAME = TrafficCop.class.getCanonicalName() + "_shared_prefs";
    private static final String PREFS_ELAPSED_SECONDS_DOWNLOAD = "elapsed_second_upload";
    private static final String PREFS_ELAPSED_SECONDS_UPLOAD = "elapsed_second_upload";
    private static final String PREFS_DOWNLOAD_BYTES = "download_bytes";
    private static final String PREFS_UPLOAD_BYTES = "upload_bytes";

    private long startTime = -1;
    private long bytesTransmitted = -1;
    private long bytesReceived = -1;
    private final List<DataUsageAlertListener> warningAdapters;
    private final Threshold downloadWarningThreshold;
    private final Threshold uploadWarningThreshold;
    private final DataUsageStatsProvider dataUsageStatsProvider;
    private final SharedPreferences prefs;

    private TrafficCop(Context context, List<DataUsageAlertListener> warningAdapters, Threshold downloadWarningThreshold, Threshold uploadWarningThreshold, DataUsageStatsProvider dataUsageStatsProvider) {
        this.dataUsageStatsProvider = dataUsageStatsProvider;
        this.warningAdapters = warningAdapters;
        this.downloadWarningThreshold = downloadWarningThreshold;
        this.uploadWarningThreshold = uploadWarningThreshold;
        this.prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Must be called in Activity.onResume();
     */
    public void onResume() {
        startTime = dataUsageStatsProvider.getNanoTime();
        bytesTransmitted = dataUsageStatsProvider.getBytesTransmitted();
        bytesReceived = dataUsageStatsProvider.getBytesReceived();
    }

    /**
     * Must be called in Activity.onPause();
     */
    public void onPause() {
        if (startTime == -1) {
            return;
        }

        int elapsedTime = (int) ((dataUsageStatsProvider.getNanoTime() - startTime) / 1000000000);
        long receivedDelta = dataUsageStatsProvider.getBytesReceived() - bytesReceived;
        long transmittedDelta = dataUsageStatsProvider.getBytesTransmitted() - bytesTransmitted;

        int elapsedTimeDownload = elapsedTime + prefs.getInt(PREFS_ELAPSED_SECONDS_DOWNLOAD, 0);
        int elapsedTimeUpload = elapsedTime + prefs.getInt(PREFS_ELAPSED_SECONDS_UPLOAD, 0);
        receivedDelta += prefs.getLong(PREFS_DOWNLOAD_BYTES, 0);
        transmittedDelta += prefs.getLong(PREFS_UPLOAD_BYTES, 0);

        DataUsage downloadUsage = new DataUsage(DataUsage.Type.DOWNLOAD, receivedDelta, elapsedTimeDownload);
        DataUsage uploadUsage = new DataUsage(DataUsage.Type.UPLOAD, transmittedDelta, elapsedTimeUpload);

        boolean hasWarnedDownload = false;
        if (downloadWarningThreshold.hasReached(downloadUsage)) {
            hasWarnedDownload = true;
            for (DataUsageAlertListener adapter : warningAdapters) {
                adapter.alertThreshold(downloadWarningThreshold, downloadUsage);
            }
        }

        boolean hasWarnedUpload = false;
        if (uploadWarningThreshold.hasReached(uploadUsage)) {
            hasWarnedUpload = true;
            for (DataUsageAlertListener adapter : warningAdapters) {
                adapter.alertThreshold(uploadWarningThreshold, uploadUsage);
            }
        }

        SharedPreferences.Editor editor = prefs.edit();

        if (hasWarnedDownload) {
            editor.remove(PREFS_DOWNLOAD_BYTES);
            editor.remove(PREFS_ELAPSED_SECONDS_DOWNLOAD);
        } else {
            editor.putInt(PREFS_ELAPSED_SECONDS_DOWNLOAD, elapsedTimeDownload);
            editor.putLong(PREFS_DOWNLOAD_BYTES, receivedDelta);
        }

        if (hasWarnedUpload) {
            editor.remove(PREFS_UPLOAD_BYTES);
            editor.remove(PREFS_ELAPSED_SECONDS_UPLOAD);
        } else {
            editor.putLong(PREFS_UPLOAD_BYTES, transmittedDelta);
            editor.putInt(PREFS_ELAPSED_SECONDS_UPLOAD, elapsedTimeUpload);
        }

        editor.apply();
    }

    /**
     * Constructs a new TrafficCop.
     */
    public static class Builder {
        private final List<DataUsageAlertListener> adapters = new ArrayList<DataUsageAlertListener>();
        private Threshold downloadWarningThreshold = Threshold.none();
        private Threshold uploadWarningThreshold = Threshold.none();
        private DataUsageStatsProvider dataUsageStatsProvider;

        /**
         * Register one or more listeners that will be called when your app's data usage goes over a threshold.
         *
         * @param listeners the listeners to register
         * @return the builder for chaining
         */
        public Builder alert(DataUsageAlertListener... listeners) {
            this.adapters.addAll(Arrays.asList(listeners));
            return this;
        }

        /**
         * Register a collection of listeners that will be called when your app's data usage goes over a threshold.
         *
         * @param listeners the listeners to register
         * @return the builder for chaining
         */
        public Builder alert(Collection<DataUsageAlertListener> listeners) {
            this.adapters.addAll(listeners);
            return this;
        }

        /**
         * Set the download threshold to be hit to notify the callback.
         *
         * @param threshold the threshold to hit
         * @return the builder for chaining
         */
        public Builder downloadWarningThreshold(Threshold threshold) {
            if (threshold == null) {
                throw new IllegalArgumentException("downloadWarningThreshold cannot be null");
            }
            downloadWarningThreshold = threshold;
            return this;
        }

        /**
         * Set the upload threshold to be hit to notify the callback.
         *
         * @param threshold the threshold to hit
         * @return the builder for caching
         */
        public Builder uploadWarningThreshold(Threshold threshold) {
            if (threshold == null) {
                throw new IllegalArgumentException("uploadWarningThreshold cannot be null");
            }
            uploadWarningThreshold = threshold;
            return this;
        }

        /**
         * Set the provider that collects the data usage stats. This does not need to be called by
         * default, but you may provide another implementation for more complex monitoring or for
         * testing.
         *
         * @param provider the provider
         * @return the builder for caching
         */
        public Builder dataUsageStatsProvider(DataUsageStatsProvider provider) {
            this.dataUsageStatsProvider = provider;
            return this;
        }

        /**
         * Construct the TrafficCop with the current configuration.
         *
         * @param context the context
         * @return the TrafficCop
         */
        public TrafficCop create(Context context) {
            if (dataUsageStatsProvider == null) {
                dataUsageStatsProvider = new DataUsageStatsProviderImpl(context.getApplicationInfo().uid);
            }
            return new TrafficCop(context.getApplicationContext(), adapters, downloadWarningThreshold, uploadWarningThreshold, dataUsageStatsProvider);
        }
    }

    private static class DataUsageStatsProviderImpl implements DataUsageStatsProvider {
        private final int uid;

        DataUsageStatsProviderImpl(int uid) {
            this.uid = uid;
        }

        @Override
        public long getNanoTime() {
            return System.nanoTime();
        }

        @Override
        public long getBytesTransmitted() {
            return TrafficStats.getUidTxBytes(uid);
        }

        @Override
        public long getBytesReceived() {
            return TrafficStats.getUidRxBytes(uid);
        }
    }
}
