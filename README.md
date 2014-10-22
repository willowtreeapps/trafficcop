[![Traffic Cop Icon](https://raw.github.com/willowtreeapps/trafficcop/master/trafficcop.png
)](https://github.com/willowtreeapps/trafficcop)

Traffic Cop
==========

Monitor your Android app's data usage so you can take action if it's over a threshold.

### Usage
Use `TrafficCop.Builder` to create an instance to use throughout your app.  This is usually done in your Application subclass.

```java
public class MyApplication extends Application {
  public void onCreate() {
    new TrafficCop.Builder()
      // Set a threshold for downloads
      .downloadWarningThreshold(Threshold.of(1, SizeUnit.GIGABYTE).per(1, TimeUnit.WEEK))
      // Set a threshold for uploads
      .uploadWarningThreshold(Threshold.of(10, SizeUnit.MEGABYTES).per(TimeUnit.HOUR))
      // Register callbacks to be alerted when the threshold is reached
      .alert(new LogDataUsageAlertAdapter(), new DataUsageAlertAdapter() {
          @Override
          public void alertThreshold(Threshold threshold, DataUsage dataUsage) {
              // Alert somehow!
          }
      })
      // Pass a string that uniquely identifies this instance. 
      .register("myTrafficCop", this);
  }
}
```

That's it!

If you want more control of when you measure you can create the `TrafficCop` instance instead of registering it.
```java
TrafficCop trafficCop = new TrafficCop.Builder()
  .downloadWarningThreshold(Threshold.of(1, SizeUnit.GIGABYTE).per(1, TimeUnit.WEEK))
  .uploadWarningThreshold(Threshold.of(10, SizeUnit.MEGABYTES).per(TimeUnit.HOUR))
  .alert(new LogDataUsageAlertAdapter(), new DataUsageAlertAdapter() {
      @Override
      public void alertThreshold(Threshold threshold, DataUsage dataUsage) {
          // Alert somehow!
      }
  })
  .create("myTrafficCop", this);
```

then call `trafficCop.startMeasuring()` and `trafficCop.stopMeasuing()` at the approprite times.
