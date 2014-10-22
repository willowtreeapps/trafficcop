package com.willowtreeapps.trafficcop.test;

import com.willowtreeapps.trafficcop.DataUsage;
import com.willowtreeapps.trafficcop.DataUsageAlertListener;
import com.willowtreeapps.trafficcop.TrafficCop;
import com.willowtreeapps.trafficcop.Threshold;
import com.willowtreeapps.trafficcop.test.helpers.TestDataUsageStatsProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.willowtreeapps.trafficcop.SizeUnit.KILOBYTES;
import static com.willowtreeapps.trafficcop.TimeUnit.SECOND;
import static com.willowtreeapps.trafficcop.TimeUnit.SECONDS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by evantatarka on 10/8/14.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TrafficCopTest {
    @Test
    public void testOnPauseUnder() {
        DataUsageAlertListener mockAdapter = mock(DataUsageAlertListener.class);
        TestDataUsageStatsProvider testProvider = new TestDataUsageStatsProvider();
        TrafficCop trafficCop = new TrafficCop.Builder()
                .downloadWarningThreshold(Threshold.of(100, KILOBYTES).per(SECOND))
                .alert(mockAdapter)
                .dataUsageStatsProvider(testProvider)
                .create("test", Robolectric.application);

        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        trafficCop.stopMeasuring();

        verify(mockAdapter, never()).alertThreshold(any(Threshold.class), any(DataUsage.class));
        trafficCop.destroy();
    }

    @Test
    public void testOnPauseOver() {
        DataUsageAlertListener mockAdapter = mock(DataUsageAlertListener.class);
        TestDataUsageStatsProvider testProvider = new TestDataUsageStatsProvider();
        Threshold threshold = Threshold.of(100, KILOBYTES).per(SECOND);
        TrafficCop trafficCop = new TrafficCop.Builder()
                .downloadWarningThreshold(threshold)
                .alert(mockAdapter)
                .dataUsageStatsProvider(testProvider)
                .create("test", Robolectric.application);

        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        testProvider.incrementReceived(100, KILOBYTES);
        trafficCop.stopMeasuring();

        verify(mockAdapter).alertThreshold(threshold, DataUsage.download(100, KILOBYTES).in(1, SECOND));
        trafficCop.destroy();
    }

    @Test
    public void testOnPauseAndResumeUnder() {
        DataUsageAlertListener mockAdapter = mock(DataUsageAlertListener.class);
        TestDataUsageStatsProvider testProvider = new TestDataUsageStatsProvider();
        TrafficCop trafficCop = new TrafficCop.Builder()
                .downloadWarningThreshold(Threshold.of(100, KILOBYTES).per(2, SECONDS))
                .alert(mockAdapter)
                .dataUsageStatsProvider(testProvider)
                .create("test", Robolectric.application);

        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        testProvider.incrementReceived(40, KILOBYTES);
        trafficCop.stopMeasuring();
        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        testProvider.incrementReceived(40, KILOBYTES);
        trafficCop.stopMeasuring();

        verify(mockAdapter, never()).alertThreshold(any(Threshold.class), any(DataUsage.class));
        trafficCop.destroy();
    }

    @Test
    public void testOnPauseAndOverUnder() throws InterruptedException {
        DataUsageAlertListener mockAdapter = mock(DataUsageAlertListener.class);
        TestDataUsageStatsProvider testProvider = new TestDataUsageStatsProvider();
        Threshold threshold = Threshold.of(100, KILOBYTES).per(2, SECONDS);
        TrafficCop trafficCop = new TrafficCop.Builder()
                .downloadWarningThreshold(threshold)
                .alert(mockAdapter)
                .dataUsageStatsProvider(testProvider)
                .create("test", Robolectric.application);

        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        testProvider.incrementReceived(50, KILOBYTES);
        trafficCop.stopMeasuring();
        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        testProvider.incrementReceived(50, KILOBYTES);
        trafficCop.stopMeasuring();

        verify(mockAdapter).alertThreshold(threshold, DataUsage.download(100, KILOBYTES).in(2, SECONDS));
        trafficCop.destroy();
    }

    @Test
    public void testOnPauseAndOverUnderOnce() throws InterruptedException {
        DataUsageAlertListener mockAdapter = mock(DataUsageAlertListener.class);
        TestDataUsageStatsProvider testProvider = new TestDataUsageStatsProvider();
        Threshold threshold = Threshold.of(100, KILOBYTES).per(1, SECONDS);
        TrafficCop trafficCop = new TrafficCop.Builder()
                .downloadWarningThreshold(threshold)
                .alert(mockAdapter)
                .dataUsageStatsProvider(testProvider)
                .create("test", Robolectric.application);

        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        testProvider.incrementReceived(100, KILOBYTES);
        trafficCop.stopMeasuring();
        trafficCop.startMeasuring();
        testProvider.incrementTime(1, SECOND);
        trafficCop.stopMeasuring();

        verify(mockAdapter, times(1)).alertThreshold(threshold, DataUsage.download(100, KILOBYTES).in(1, SECONDS));
        trafficCop.destroy();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatingMultipleWithSameIdFails() {
        new TrafficCop.Builder().create("test", Robolectric.application);
        new TrafficCop.Builder().create("test", Robolectric.application);
    }
}
