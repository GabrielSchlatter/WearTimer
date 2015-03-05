package com.cologne.hackaton.wearstopwatch.activity.timer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.cologne.hackaton.wearstopwatch.R;
import com.cologne.hackaton.wearstopwatch.activity.event.RequestTimerStatusEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.ResetTimerEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.StartTimerEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.StopTimerEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.TimerAlarmEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.TimerStartedEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.TimerStatusResponseEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.TimerTickEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by admin on 3/4/2015.
 */
public class TimerActivity extends Activity {

  private EventBus eventBus = EventBus.getDefault();

  boolean mIsTimerRunning;

  private Button mBtnStartStop;
  private NumberPicker npMinutes;
  private NumberPicker npSeconds;

  private int mMinutes;
  private int mSeconds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    eventBus.register(this);

    setContentView(R.layout.activity_timer);

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        initializeViews();

        if (!isMyServiceRunning(TimerService.class)) {
          Log.d(getClass().getSimpleName(), "Start Timer Service");
          Intent i = new Intent(TimerActivity.this, TimerService.class);
          startService(i);
        }
        else {
          // Attach to existing Service
          Log.d(getClass().getSimpleName(), "Attach Listeners");
          eventBus.post(new RequestTimerStatusEvent());
        }
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    eventBus.unregister(this);
  }

  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager
        .getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }

  private void initializeViews() {
    npMinutes = (NumberPicker) findViewById(R.id.np_minutes);
    npMinutes.setMaxValue(99);
    npMinutes.setMinValue(0);
    npMinutes.setWrapSelectorWheel(false);
    // npMinutes.setFormatter(new TimerFormatter());

    npSeconds = (NumberPicker) findViewById(R.id.np_seconds);
    npSeconds.setMaxValue(59);
    npSeconds.setMinValue(0);
    npSeconds.setWrapSelectorWheel(false);
    // npSeconds.setFormatter(new TimerFormatter());

    mBtnStartStop = (Button) findViewById(R.id.btn_start);
    mBtnStartStop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mIsTimerRunning) {
          mSeconds = npSeconds.getValue();
          mMinutes = npMinutes.getValue();
          eventBus.post(new StartTimerEvent(mSeconds, mMinutes));
        }
        else {
          eventBus.post(new StopTimerEvent());
        }
      }
    });
  }

  public void onEvent(TimerTickEvent event) {
    int secs = (int) (event.getMillisUntil() / 1000);
    int mins = secs / 60;
    secs = secs % 60;
    Log.d(getClass().getSimpleName(), "Tick: " + mins + " : " + secs);
    npMinutes.setMinValue(mins);
    npMinutes.setMaxValue(mins);
    npSeconds.setMinValue(secs);
    npSeconds.setMaxValue(secs);
  }

  public void onEvent(TimerStartedEvent event) {
    npMinutes.setEnabled(false);
    npSeconds.setEnabled(false);
    mBtnStartStop.setText(getString(R.string.stop));
    mIsTimerRunning = true;
  }

  public void onEvent(ResetTimerEvent event) {
    npMinutes.setEnabled(true);
    npMinutes.setMaxValue(99);
    npMinutes.setMinValue(0);
    npSeconds.setEnabled(true);
    npSeconds.setMaxValue(59);
    npSeconds.setMinValue(0);
    npSeconds.setValue(mSeconds);
    npMinutes.setValue(mMinutes);
    mBtnStartStop.setText(getString(R.string.start));
    mIsTimerRunning = false;
  }

  public void onEvent(TimerStatusResponseEvent event) {
    if (event.isRunning()) {
      npMinutes.setEnabled(false);
      npSeconds.setEnabled(false);

      // npSeconds.setFormatter(new TimerFormatter());
      // npMinutes.setFormatter(new TimerFormatter());

      mBtnStartStop.setText(getString(R.string.stop));
      mIsTimerRunning = true;
    }
    else {
      mIsTimerRunning = false;
    }
  }

  public void onEvent(TimerAlarmEvent event) {
    npMinutes.setEnabled(true);
    npMinutes.setMaxValue(99);
    npMinutes.setMinValue(0);
    npSeconds.setEnabled(true);
    npSeconds.setMaxValue(59);
    npSeconds.setMinValue(0);
    npSeconds.setValue(mSeconds);
    npMinutes.setValue(mMinutes);
    mBtnStartStop.setText(getString(R.string.start));
    mIsTimerRunning = false;
    Toast.makeText(this, "ALARM!!", Toast.LENGTH_SHORT).show();
  }

  public class TimerFormatter implements NumberPicker.Formatter {
    public String format(int value) {
      return String.format("%02d", value);
    }
  }
}
