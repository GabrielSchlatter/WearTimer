package com.cologne.hackaton.wearstopwatch.activity.timer;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
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
 * Created by admin on 3/5/2015.
 */
public class TimerService extends Service {

  private EventBus eventBus = EventBus.getDefault();

  private static final long SECOND = 1000;
  private static final long MINUTE = 60000;

  private CountDownTimer mTimer;
  private boolean mIsRunning;

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(getClass().getSimpleName(), "Started");
    eventBus.register(this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(getClass().getSimpleName(), "Destroyed");
    eventBus.unregister(this);
  }

  public void onEvent(StartTimerEvent event) {
    Log.d(getClass().getSimpleName(), "Start Timer");
    start(event.getSeconds() * SECOND + event.getMinutes() * MINUTE);
  }

  public void onEvent(StopTimerEvent event) {
    Log.d(getClass().getSimpleName(), "Stop Timer");
    stop();
  }

  private void start(long timeSpan) {
    mTimer = new CountDownTimer(timeSpan, SECOND) {
      @Override
      public void onTick(long millisUntilFinished) {
        eventBus.post(new TimerTickEvent(millisUntilFinished));
      }

      @Override
      public void onFinish() {
        reset();
        alarm();
      }
    };
    mTimer.start();
    eventBus.post(new TimerStartedEvent());
    mIsRunning = true;
  }

  private void stop() {
    if (mTimer != null)
      mTimer.cancel();
    reset();
  }

  private void alarm() {
    eventBus.post(new TimerAlarmEvent());
  }

  private void reset() {
    eventBus.post(new ResetTimerEvent());
  }

  public void onEvent(RequestTimerStatusEvent event) {
    eventBus.post(new TimerStatusResponseEvent(mIsRunning));
  }
}
