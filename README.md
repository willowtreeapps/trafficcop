Traffic Cop
==========

Monitor your Android app's data usage so you can take action if it's over a threshold.

### Usage
Use `TrafficCop.Builder` to create an insance to use throughout your app. (Usually in your Application sublcass).

```java
public class MyApplication extends Application {
  private static TrafficCop sTrafficCop;
  
  public void onCreate() {
    sTrafficCop = new TrafficCop.Builder()
                  // Set a threshold for downloads
                  .downloadWarningThreashold(Threshold.of(1, SizeUnit.GIGABYTE).per(1, TimeUnit.WEEK))
                  // Set a threshold for uploads
                  .uploadWarningThreshold(Threshold.of(10, SizeUnit.MEGABYTES).per(TimeUnit.HOUR))
                  // Register callbacks to be aletered when the threshold is hit
                  .alert(new LogDataUsageAlertAdapter(), new DataUsageAlertAdapter() {
                      @Override
                      public void alertThreshold(Threshold threshold, DataUsage dataUsage) {
                          // Alert somehow!
                      }
                  })
                  .create(this);
  }
  
  public static TrafficCop getTrafficCop() {
    return sTrafficCop;
  }
}
```

Then is your base acitivty, call `onPause()` and `onResume()`.

```java
public class MyBaseActivity extends Activity {
  @Override
  protected void onPause() {
    super.onPause();
    MyApplication.getTrafficCop().onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    MyApplication.getTrafficCop().onResume();
  }
}
```

Alternatively, you can save some work by registering it with the application context.
```java
public class MyApplication extends Application {
  public void onCreate() {
    new TrafficCop.Builder()
    ...
    .register(this);
  }
}
```

That's it!
