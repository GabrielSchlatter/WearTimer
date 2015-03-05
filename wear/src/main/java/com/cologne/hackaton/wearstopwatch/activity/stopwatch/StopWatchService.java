package com.cologne.hackaton.wearstopwatch.activity.stopwatch;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.cologne.hackaton.wearstopwatch.activity.event.AttachListenersEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.CreateLapEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.LapUpdateEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.PauseStopwatchEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.RequestStatusEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.ResetStopWatchEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.SaveLapsEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.StartStopwatchEvent;
import com.cologne.hackaton.wearstopwatch.activity.event.StatusResponseEvent;
import com.gabriel.android.timelib.StopWatch;
import com.gabriel.android.timelib.model.Lap;

import de.greenrobot.event.EventBus;

/**
 * Created by gabriel on 3/5/2015.
 */
public class StopWatchService extends Service {

  private EventBus eventBus = EventBus.getDefault();
  private StopWatch stopWatch;
  private List<Lap> mLaps = new ArrayList<>();

  private StartStopwatchEvent.StopWatchTickCallback tickCallback;

  @Override
  public void onCreate() {
    super.onCreate();
    eventBus.register(this);
    Log.d(getClass().getSimpleName(), "StopWatchService started");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    eventBus.unregister(this);
    Log.d(getClass().getSimpleName(), "StopWatchService destroyed");
  }

  public void onEvent(final StartStopwatchEvent event) {
    Log.d(getClass().getSimpleName(), "StartStopwatchEvent");
    tickCallback = event.getCallback();
    if (stopWatch == null) {
      stopWatch = new StopWatch(new StopWatch.TimeChangedCallback() {
        @Override
        public void timeChanged(long time) {
          tickCallback.onTick(time);
        }
      }, new StopWatch.LapsChangedCallback() {
        @Override
        public void lapsChanged(Lap lap) {
          if (lap != null) {
            mLaps.add(lap);
            eventBus.post(new LapUpdateEvent(mLaps));
          }
          else {
            eventBus.post(new SaveLapsEvent());
            mLaps.clear();
            eventBus.post(new LapUpdateEvent(mLaps));
          }
        }
      });
    }
    stopWatch.startStopWatch();
  }

  public void onEvent(PauseStopwatchEvent event) {
    stopWatch.pauseStopWatch();
    event.getCallback().receivePauseTime(stopWatch.getCurrentTime());
  }

  public void onEvent(ResetStopWatchEvent event) {
    Log.d(getClass().getSimpleName(), "ResetStopWatchEvent");
    eventBus.post(new LapUpdateEvent(mLaps));
    stopWatch.resetStopWatch();
  }

  public void onEvent(CreateLapEvent event) {
    Log.d(getClass().getSimpleName(), "CreateLapEvent");
    stopWatch.saveLap();
  }

  public void onEvent(AttachListenersEvent event) {
    tickCallback = event.getTickCallback();
    eventBus.post(new LapUpdateEvent(mLaps));
  }

  public void onEvent(RequestStatusEvent event) {
    eventBus.post(new StatusResponseEvent(stopWatch.isRunning()));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

}
