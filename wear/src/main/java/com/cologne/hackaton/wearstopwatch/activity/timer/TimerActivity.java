package com.cologne.hackaton.wearstopwatch.activity.timer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.cologne.hackaton.wearstopwatch.R;

/**
 * Created by admin on 3/4/2015.
 */
public class TimerActivity extends Activity {

  private static final long SECOND = 1000;
  private static final long MINUTE = 60000;
  boolean mIsTimerRunning;

  private Button mBtnStartStop;
  private NumberPicker npMinutes;
  private NumberPicker npSeconds;

  private CountDownTimer mTimer;
  private int mMinutes;
  private int mSeconds;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_timer);

    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        initializeViews();
      }
    });
  }

  private void initializeViews() {
    npMinutes = (NumberPicker) findViewById(R.id.np_minutes);
    npMinutes.setMaxValue(99);
    npMinutes.setMinValue(0);
    npMinutes.setWrapSelectorWheel(false);
    npMinutes.setFormatter(new TimerFormatter());

    npSeconds = (NumberPicker) findViewById(R.id.np_seconds);
    npSeconds.setMaxValue(59);
    npSeconds.setMinValue(0);
    npSeconds.setWrapSelectorWheel(false);
    npSeconds.setFormatter(new TimerFormatter());

    mBtnStartStop = (Button) findViewById(R.id.btn_start);
    mBtnStartStop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mIsTimerRunning) {
          mSeconds = npSeconds.getValue();
          mMinutes = npMinutes.getValue();
          start(npSeconds.getValue() * SECOND + npMinutes.getValue() * MINUTE);
        }
        else {
          stop();
        }
      }
    });

  }

  private void start(long timeSpan) {
    mTimer = new CountDownTimer(timeSpan, SECOND) {
      @Override
      public void onTick(long millisUntilFinished) {
        int secs = (int) (millisUntilFinished / 1000);
        int mins = secs / 60;
        secs = secs % 60;
        npMinutes.setMinValue(mins);
        npMinutes.setMaxValue(mins);
        npSeconds.setMinValue(secs);
        npSeconds.setMaxValue(secs);
      }

      @Override
      public void onFinish() {
        reset();
        alarm();
      }
    };
    npMinutes.setEnabled(false);
    npSeconds.setEnabled(false);
    mBtnStartStop.setText(getString(R.string.stop));
    mTimer.start();
    mIsTimerRunning = true;
  }

  private void stop() {
    if (mTimer != null)
      mTimer.cancel();
    reset();
  }

  private void alarm() {
    Toast.makeText(this, "TIMER FINISHED", Toast.LENGTH_SHORT).show();
  }

  private void reset() {
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

  public class TimerFormatter implements NumberPicker.Formatter {
    public String format(int value) {
      return String.format("%02d", value);
    }
  }
}
