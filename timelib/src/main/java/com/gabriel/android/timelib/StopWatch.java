package com.gabriel.android.timelib;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.gabriel.android.timelib.model.Lap;

/**
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class StopWatch {

  // Async
  private Handler mHandler;
  private Runnable mUpdaterTask;

  // Time
  private long mLastLapTime = 0L;
  private long mStartTime = 0L;
  private long mTimeInMilliseconds = 0L;
  private long mTimeSwapBuffer = 0L;
  private long mUpdatedTime = 0L;

  // Laps
  private List<Lap> mLaps = new ArrayList<>();

  // Callbacks
  private TimeChangedCallback mTimeChangedCallback;
  private LapsChangedCallback mLapsChangedCallback;

  // Status
  private boolean mRunning;

  public StopWatch(TimeChangedCallback timeChangedCallback,
      LapsChangedCallback lapsChangedCallback) {

    mTimeChangedCallback = timeChangedCallback;
    mLapsChangedCallback = lapsChangedCallback;

    mHandler = new Handler();
    mUpdaterTask = new Runnable() {
      @Override
      public void run() {
        mTimeInMilliseconds = System.currentTimeMillis() - mStartTime;
        mUpdatedTime = mLastLapTime + mTimeSwapBuffer + mTimeInMilliseconds;
        mTimeChangedCallback.timeChanged(mUpdatedTime);
        mHandler.postDelayed(this, 0);
      }
    };
  }

  /**
   * Launches stop watch timer
   */
  public void startStopWatch() {
    mStartTime = System.currentTimeMillis();
    mHandler.post(mUpdaterTask);
    mRunning = true;
  }

  /**
   * Pauses stopwatch timer
   */
  public void pauseStopWatch() {
    mTimeSwapBuffer += mTimeInMilliseconds;
    mHandler.removeCallbacks(mUpdaterTask);
    mRunning = false;
  }

  /**
   * Does saving the lap time
   */
  public void saveLap() {

    long lapTime = mUpdatedTime - mLastLapTime;
    Lap newLap = new Lap(mLaps.size() + 1, lapTime, mUpdatedTime);
    mLaps.add(newLap);
    mLastLapTime = mUpdatedTime;
    mLapsChangedCallback.lapsChanged(newLap);
    mStartTime = System.currentTimeMillis();
    mTimeInMilliseconds = 0L;
    mTimeSwapBuffer = 0L;
    mUpdatedTime = 0L;
  }

  /**
   * Resets stop watch timer
   */
  public void resetStopWatch() {
    mLastLapTime = 0L;
    mStartTime = 0L;
    mTimeInMilliseconds = 0L;
    mTimeSwapBuffer = 0L;
    mUpdatedTime = 0L;
    mLaps.clear();
    mLapsChangedCallback.lapsChanged(null);
    mTimeChangedCallback.timeChanged(mUpdatedTime);
  }

  public boolean isRunning() {
    return mRunning;
  }

  public interface TimeChangedCallback {
    public void timeChanged(long time);
  }

  public interface LapsChangedCallback {
    public void lapsChanged(Lap lap);
  }

}
