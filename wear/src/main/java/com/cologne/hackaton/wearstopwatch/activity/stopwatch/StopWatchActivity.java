package com.cologne.hackaton.wearstopwatch.activity.stopwatch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.cologne.hackaton.wearstopwatch.R;
import com.gabriel.android.timelib.StopWatch;
import com.gabriel.android.timelib.model.Lap;
import com.gabriel.android.timelib.utils.StringUtils;

/**
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class StopWatchActivity extends Activity {

  private StopWatch stopWatch;

  // Views
  private TextView mIimeView;
  private ImageButton mBtnStart;
  private ImageButton mBtnStop;
  private ImageButton mBtnLock;

  // Statuses
  private boolean mLocked;

  // Laps
  private List<Lap> mLaps = new ArrayList<>();
  private LapAdapter mLapsAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_stopwatch);

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        initializeViews();

        // Init StopWatch
        stopWatch = new StopWatch(new StopWatch.TimeChangedCallback() {
          @Override
          public void timeChanged(long time) {
            String formattedTime = StringUtils.formatString(time);
            mIimeView.setText(formattedTime);
          }
        }, new StopWatch.LapsChangedCallback() {
          @Override
          public void lapsChanged(Lap lap) {
            if (lap != null) {
              mLaps.add(0, lap);
            }
            else {
              mLaps.clear();
            }
            mLapsAdapter.notifyDataSetChanged();
          }
        });
      }
    });
  }

  /**
   * Initializes view controls
   */
  private void initializeViews() {
    mIimeView = (TextView) findViewById(R.id.tv_time);

    ListView mLapsListView = (ListView) findViewById(R.id.lv_laps);
    mLapsAdapter = new LapAdapter(this, android.R.layout.simple_list_item_1,
        mLaps);
    mLapsListView.setAdapter(mLapsAdapter);

    // start button
    mBtnStart = (ImageButton) findViewById(R.id.ib_start);
    mBtnStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mLocked) {
          handleStartButton();
        }
      }
    });

    // stop button
    mBtnStop = (ImageButton) findViewById(R.id.ib_stop);
    mBtnStop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mLocked) {
          handleStopButton();
        }
      }
    });

    // lock button
    mBtnLock = (ImageButton) findViewById(R.id.ib_lock);
    mBtnLock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleLockAction();
      }
    });
  }

  /**
   * Handles the action Start
   */
  private void handleStartButton() {
    if (!stopWatch.isRunning()) {
      stopWatch.startStopWatch();

      // Change View state
      mBtnStart.setImageDrawable(getResources().getDrawable(
          R.mipmap.ic_action_playback_pause));
      mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
      mBtnStop.setImageDrawable(getResources().getDrawable(
          R.mipmap.ic_action_add));
      mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));

    }
    else {
      stopWatch.pauseStopWatch();

      // Change View state
      mBtnStart.setImageDrawable(getResources().getDrawable(
          R.mipmap.ic_action_playback_play));
      mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
      mBtnStop.setImageDrawable(getResources().getDrawable(
          R.mipmap.ic_action_reload));
      mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
    }
  }

  /**
   * Handles the action Stop
   */
  private void handleStopButton() {
    if (!stopWatch.isRunning()) {
      stopWatch.resetStopWatch();
    }
    else {
      Log.d(getClass().getSimpleName(), "save lap");
      stopWatch.saveLap();
    }
  }

  /**
   * Handles the action Lock what appropriate button pressed
   */
  private void handleLockAction() {
    if (!mLocked) {
      // Lock
      mBtnLock.setImageDrawable(getResources().getDrawable(
          R.mipmap.ic_action_lock_closed));
      mBtnStop.setBackgroundColor(getResources().getColor(R.color.grey));
      mBtnStart.setBackgroundColor(getResources().getColor(R.color.light_grey));
      mBtnStop.setBackgroundColor(getResources().getColor(R.color.light_grey));
      mLocked = true;
    }
    else {
      // Unlock
      mBtnLock.setImageDrawable(getResources().getDrawable(
          R.mipmap.ic_action_lock_open));
      if (stopWatch.isRunning()) {
        mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
      }
      else {
        mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
        mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));
      }
      mLocked = false;
    }
  }
}