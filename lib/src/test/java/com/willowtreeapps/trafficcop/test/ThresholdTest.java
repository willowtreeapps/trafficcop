package com.willowtreeapps.trafficcop.test;

import com.willowtreeapps.trafficcop.DataUsage;
import com.willowtreeapps.trafficcop.Threshold;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.willowtreeapps.trafficcop.SizeUnit.GIGABYTES;
import static com.willowtreeapps.trafficcop.SizeUnit.KILOBYTES;
import static com.willowtreeapps.trafficcop.TimeUnit.SECOND;
import static com.willowtreeapps.trafficcop.TimeUnit.SECONDS;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by evantatarka on 10/8/14.
 */
@RunWith(JUnit4.class)
public class ThresholdTest {
    @Test
    public void testNone() {
        Threshold threshold = Threshold.none();
        DataUsage usage = DataUsage.download(10, GIGABYTES).in(1, SECOND);
        assertThat(threshold.hasReached(usage)).isFalse();
    }

    @Test
    public void testUnderBytes() {
        Threshold threshold = Threshold.of(100, KILOBYTES).per(SECOND);
        DataUsage usage = DataUsage.download(99, KILOBYTES).in(1, SECOND);
        assertThat(threshold.hasReached(usage)).isFalse();
    }

    @Test
    public void testOverBytes() {
        Threshold threshold = Threshold.of(100, KILOBYTES).per(SECOND);
        DataUsage usage = DataUsage.download(101, KILOBYTES).in(1, SECOND);
        assertThat(threshold.hasReached(usage)).isTrue();
    }

    @Test
    public void testUnderTimeAndOverBytes() {
        Threshold threshold = Threshold.of(100, KILOBYTES).per(4, SECONDS);
        DataUsage usage = DataUsage.download(100, KILOBYTES).in(1, SECOND);
        assertThat(threshold.hasReached(usage)).isTrue();
    }

    @Test
    public void testOverTimeAndUnderBytes() {
        Threshold threshold = Threshold.of(100, KILOBYTES).per(SECOND);
        DataUsage usage = DataUsage.download(99, KILOBYTES).in(4, SECONDS);
        assertThat(threshold.hasReached(usage)).isFalse();
    }

    @Test
    public void testOverTimeAndRelativelyUnderBytes() {
        Threshold threshold = Threshold.of(100, KILOBYTES).per(SECOND);
        DataUsage usage = DataUsage.download(399, KILOBYTES).in(4, SECONDS);
        assertThat(threshold.hasReached(usage)).isFalse();
    }

    @Test
    public void testOverTimeAndRelativelyOverBytes() {
        Threshold threshold = Threshold.of(100, KILOBYTES).per(SECOND);
        DataUsage usage = DataUsage.download(401, KILOBYTES).in(4, SECONDS);
        assertThat(threshold.hasReached(usage)).isTrue();
    }
}
