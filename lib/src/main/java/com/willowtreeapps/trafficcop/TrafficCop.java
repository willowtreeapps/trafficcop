package com.willowtreeapps.trafficcop;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A simple way to get detect data usage over a threshold.
 */
public class TrafficCop {
    private static final List<String> TRAFFIC_COP_IDS = new ArrayList<String>();
    private static final String SHARED_PREFS_NAME = TrafficCop.class.getCanonicalName() + "_shared_prefs";
    private static final String PREFS_ELAPSED_SECONDS_DOWNLOAD = "elapsed_second_upload";
    private static final String PREFS_ELAPSED_SECONDS_UPLOAD = "elapsed_second_upload";
    private static final String PREFS_DOWNLOAD_BYTES = "download_bytes";
    private static final String PREFS_UPLOAD_BYTES = "upload_bytes";

    private long startTime = -1;
    private long bytesTransmitted = -1;
    private long bytesReceived = -1;
    private final String id;
    private final List<DataUsageAlertListener> dataUsageAlertListeners;
    private final Threshold downloadWarningThreshold;
    private final Threshold uploadWarningThreshold;
    private final DataUsageStatsProvider dataUsageStatsProvider;
    private final SharedPreferences prefs;
    private Application application;
    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks;
    private boolean isDestroyed;

    private TrafficCop(Context context, String id, List<DataUsageAlertListener> dataUsageAlertListeners, Threshold downloadWarningThreshold, Threshold uploadWarningThreshold, DataUsageStatsProvider dataUsageStatsProvider) {
        this.id = id;
        this.dataUsageStatsProvider = dataUsageStatsProvider;
        this.dataUsageAlertListeners = dataUsageAlertListeners;
        this.downloadWarningThreshold = downloadWarningThreshold;
        this.uploadWarningThreshold = uploadWarningThreshold;
        this.prefs = context.getSharedPreferences(SHARED_PREFS_NAME + id, Context.MODE_PRIVATE);
    }

    /**
     * Starts measuring data usage. If you are using
     * {@link TrafficCop#register(android.app.Application)} then you don't need to call this.
     */
    public void startMeasuring() {
        if (isDestroyed) {
            throw new IllegalStateException("The TrafficCop has been destroyed.");
        }

        startTime = dataUsageStatsProvider.getNanoTime();
        bytesTransmitted = dataUsageStatsProvider.getBytesTransmitted();
        bytesReceived = dataUsageStatsProvider.getBytesReceived();
    }

    /**
     * Stops measuring data usage and possible triggers the listeners if a threshold is met. If you
     * are using {@link TrafficCop#register(android.app.Application)} then you don't need to call
     * this.
     */
    public void stopMeasuring() {
        if (isDestroyed) {
            throw new IllegalStateException("The TrafficCop has been destroyed.");
        }

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
            for (DataUsageAlertListener adapter : dataUsageAlertListeners) {
                adapter.alertThreshold(downloadWarningThreshold, downloadUsage);
            }
        }

        boolean hasWarnedUpload = false;
        if (uploadWarningThreshold.hasReached(uploadUsage)) {
            hasWarnedUpload = true;
            for (DataUsageAlertListener adapter : dataUsageAlertListeners) {
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
     * Register the TrafficCop to the activity lifecycle. If you call this, you don't need to call
     * {@link #stopMeasuring()}/{@link #startMeasuring()}.
     *
     * @param application the application context.
     */
    public void register(final Application application) {
        if (isDestroyed) {
            throw new IllegalStateException("The TrafficCop has been destroyed.");
        }

        this.application = application;
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                startMeasuring();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                stopMeasuring();
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    /**
     * Unregister the TrafficCop from the activity lifecycle. You may call this after
     * {@link #register(android.app.Application)} if you no longer want to be notified.
     */
    public void unregister() {
        if (activityLifecycleCallbacks != null) {
            application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
            activityLifecycleCallbacks = null;
            application = null;
        }
    }

    /**
     * Destroys the TrafficCop. You cannot call any other methods on this traffic cop after calling
     * this method, but you are now free to create another one with the same id.
     */
    public void destroy() {
        isDestroyed = true;
        TRAFFIC_COP_IDS.remove(id);
        prefs.edit().clear().apply();
        unregister();
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
        public TrafficCop create(String id, Context context) {
            if (TRAFFIC_COP_IDS.contains(id)) {
                throw new IllegalArgumentException("A TrafficCop with id '" + id + "' has already been created.");
            }
            TRAFFIC_COP_IDS.add(id);

            if (dataUsageStatsProvider == null) {
                dataUsageStatsProvider = new DataUsageStatsProviderImpl(context.getApplicationInfo().uid);
            }
            return new TrafficCop(context.getApplicationContext(), id, adapters, downloadWarningThreshold, uploadWarningThreshold, dataUsageStatsProvider);
        }

        /**
         * Construct the TrafficCop with the current configuration and register it to the activity
         * lifecycle.
         *
         * @param application the application context.
         * @return the TrafficCop
         * @see TrafficCop#register(android.app.Application)
         */
        public TrafficCop register(String id, Application application) {
            TrafficCop trafficCop = create(id, application);
            trafficCop.register(application);
            return trafficCop;
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
