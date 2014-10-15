package com.willowtreeapps.trafficcop;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by evantatarka on 10/7/14.
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
    private final List<DataUsageAlertAdapter> warningAdapters;
    private final Threshold downloadWarningThreshold;
    private final Threshold uploadWarningThreshold;
    private final DataUsageStatsProvider dataUsageStatsProvider;
    private final SharedPreferences prefs;

    private TrafficCop(Context context, List<DataUsageAlertAdapter> warningAdapters, Threshold downloadWarningThreshold, Threshold uploadWarningThreshold, DataUsageStatsProvider dataUsageStatsProvider) {
        this.dataUsageStatsProvider = dataUsageStatsProvider;
        this.warningAdapters = warningAdapters;
        this.downloadWarningThreshold = downloadWarningThreshold;
        this.uploadWarningThreshold = uploadWarningThreshold;
        this.prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void onResume() {
        startTime = dataUsageStatsProvider.getNanoTime();
        bytesTransmitted = dataUsageStatsProvider.getBytesTransmitted();
        bytesReceived = dataUsageStatsProvider.getBytesReceived();
    }

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
            for (DataUsageAlertAdapter adapter : warningAdapters) {
                adapter.alertThreshold(downloadWarningThreshold, downloadUsage);
            }
        }

        boolean hasWarnedUpload = false;
        if (uploadWarningThreshold.hasReached(uploadUsage)) {
            hasWarnedUpload = true;
            for (DataUsageAlertAdapter adapter : warningAdapters) {
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

    public static class Builder {
        private final List<DataUsageAlertAdapter> adapters = new ArrayList<DataUsageAlertAdapter>();
        private Threshold downloadWarningThreshold = Threshold.none();
        private Threshold uploadWarningThreshold = Threshold.none();
        private DataUsageStatsProvider dataUsageStatsProvider;

        public Builder alert(DataUsageAlertAdapter... adapters) {
            this.adapters.addAll(Arrays.asList(adapters));
            return this;
        }

        public Builder alert(Collection<DataUsageAlertAdapter> adapters) {
            this.adapters.addAll(adapters);
            return this;
        }

        public Builder downloadWarningThreashold(Threshold threshold) {
            if (threshold == null) {
                throw new IllegalArgumentException("downloadWarningThreshold cannot be null");
            }
            downloadWarningThreshold = threshold;
            return this;
        }

        public Builder uploadWarningThreshold(Threshold threshold) {
            if (threshold == null) {
                throw new IllegalArgumentException("uploadWarningThreshold cannot be null");
            }
            uploadWarningThreshold = threshold;
            return this;
        }

        public Builder dataUsageStatsProvider(DataUsageStatsProvider provider) {
            this.dataUsageStatsProvider = provider;
            return this;
        }

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
