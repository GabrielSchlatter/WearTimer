package com.android.gabriel.wearstopwatch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

  // Views
  private TextView tv_time;
  private ImageButton ibStart;
  private ImageButton ibStop;
  private ImageButton ibLock;
  private ListView lvLaps;
  // Status
  private boolean locked;
  private boolean running;
  // Async
  private Handler handler;
  // Time
  private long lastLapTime = 0L;
  private long startTime = 0L;
  private long timeInMilliseconds = 0L;
  private long timeSwapBuffer = 0L;
  private long updatedTime = 0L;
  // Laps
  private ArrayList<Lap> laps = new ArrayList<>();
  private LapAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    handler = new Handler();
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        tv_time = (TextView) findViewById(R.id.tv_time);
        ibStart = (ImageButton) findViewById(R.id.ib_start);
        ibStop = (ImageButton) findViewById(R.id.ib_stop);
        ibLock = (ImageButton) findViewById(R.id.ib_lock);

        initializeViews();
      }
    });
  }

  final Runnable updater = new Runnable() {
    @Override
    public void run() {
      timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
      updatedTime = lastLapTime + timeSwapBuffer + timeInMilliseconds;

      refreshTimeText();
      handler.postDelayed(this, 0);

    }
  };

  private void initializeViews() {
    refreshTimeText();
    lvLaps = (ListView) findViewById(R.id.lv_laps);
    adapter = new LapAdapter(this, android.R.layout.simple_list_item_1, laps);
    lvLaps.setAdapter(adapter);
    ibStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!locked) {
          if (!running) {
            // Start
            ibStart.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_playback_pause));
            ibStart.setBackgroundColor(getResources().getColor(R.color.orange));
            ibStop.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_add));
            ibStop.setBackgroundColor(getResources().getColor(R.color.blue));
            StartStopWatch();
            running = true;
          }
          else {
            // Pause
            ibStart.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_playback_play));
            ibStart.setBackgroundColor(getResources().getColor(R.color.green));
            ibStop.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_reload));
            ibStop.setBackgroundColor(getResources()
                .getColor(R.color.dark_blue));
            PauseStopWatch();
            running = false;
          }
        }
      }
    });

    ibStop.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!locked) {
          if (!running) {
            ResetStopWatch();
          }
          else {
            SaveLap();
          }
        }
      }
    });

    ibLock.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!locked) {
          // Lock
          ibLock.setImageDrawable(getResources().getDrawable(
              R.mipmap.ic_action_lock_closed));
          ibStop.setBackgroundColor(getResources().getColor(R.color.grey));
          ibStart.setBackgroundColor(getResources()
              .getColor(R.color.light_grey));
          ibStop
              .setBackgroundColor(getResources().getColor(R.color.light_grey));
          locked = true;
        }
        else {
          // Unlock
          ibLock.setImageDrawable(getResources().getDrawable(
              R.mipmap.ic_action_lock_open));
          if (running) {
            ibStop.setBackgroundColor(getResources()
                .getColor(R.color.dark_blue));
            ibStart.setBackgroundColor(getResources().getColor(R.color.orange));
          }
          else {
            ibStart.setBackgroundColor(getResources().getColor(R.color.green));
            ibStop.setBackgroundColor(getResources().getColor(R.color.blue));
          }
          locked = false;
        }
      }
    });
  }

  public String getTimeString(long time) {
    int secs = (int) (time / 1000);
    int mins = secs / 60;
    secs = secs % 60;
    int milliseconds = (int) (time % 1000);
    return "" + mins + ":" + String.format("%02d", secs) + ":"
        + String.format("%03d", milliseconds);
  }

  private void refreshTimeText() {
    tv_time.setText(getTimeString(updatedTime));
  }

  private void SaveLap() {
    laps.add(0, new Lap(updatedTime - lastLapTime));
    lastLapTime = updatedTime;
    adapter.notifyDataSetChanged();
    startTime = SystemClock.uptimeMillis();
    timeInMilliseconds = 0L;
    timeSwapBuffer = 0L;
    updatedTime = 0L;
  }

  private void ResetStopWatch() {
    lastLapTime = 0L;
    startTime = 0L;
    timeInMilliseconds = 0L;
    timeSwapBuffer = 0L;
    updatedTime = 0L;
    adapter.clear();
    refreshTimeText();
  }

  private void PauseStopWatch() {
    timeSwapBuffer += timeInMilliseconds;
    handler.removeCallbacks(updater);
  }

  private void StartStopWatch() {
    startTime = SystemClock.uptimeMillis();
    handler.post(updater);
  }

  public class LapAdapter extends ArrayAdapter<Lap> {

    public LapAdapter(Context context, int resource, List<Lap> objects) {
      super(context, resource, objects);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      if (convertView == null) {
        convertView = getLayoutInflater().inflate(R.layout.lap_list_item, null);
        LapHolder holder = new LapHolder();
        holder.tvText = (TextView) convertView.findViewById(R.id.tv_text);
        convertView.setTag(holder);
      }

      LapHolder holder = (LapHolder) convertView.getTag();
      holder.tvText.setText(getTimeString(getItem(position).getTime()));
      return convertView;
    }

    private class LapHolder {
      private TextView tvText;
    }
  }

  private class Lap {
    private long time;

    public Lap(long time) {
      this.time = time;
    }

    public long getTime() {
      return time;
    }
  }
}
