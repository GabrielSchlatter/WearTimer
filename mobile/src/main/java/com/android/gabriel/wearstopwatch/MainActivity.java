package com.android.gabriel.wearstopwatch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements DataApi.DataListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

  private static final String START_STOPWATCH_PATH = "start/stopwatch";
  private static final String SAVE_LAP_MESSAGE = "save/laptime";
  private static final String RESET_STOPWATCH = "reset/watch";
  private static final String PAUSE_WATCH = "pause/watch";
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
  // Wear Api
  private GoogleApiClient googleApiClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    handler = new Handler();

    tv_time = (TextView) findViewById(R.id.tv_time);
    ibStart = (ImageButton) findViewById(R.id.ib_start);
    ibStop = (ImageButton) findViewById(R.id.ib_stop);
    ibLock = (ImageButton) findViewById(R.id.ib_lock);

    initializeViews();

    // Create a GoogleApiClient instance
    googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
        .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
        .build();
  }

  @Override
  protected void onStart() {
    super.onStart();
    googleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    Wearable.DataApi.removeListener(googleApiClient, this);
    Wearable.MessageApi.removeListener(googleApiClient, this);
    googleApiClient.disconnect();
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
                        StartStopWatch(0, true);
                    }
                    else {
                        // Pause
                        PauseStopWatch(0, true);
                    }
                }
            }
        });

        ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locked) {
                    if (!running) {
                        ResetStopWatch(true);
                    }
                    else {
                        SaveLap(0, true);
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

    private void SaveLap(long lapTime, boolean sendMessage) {

        if (lapTime == 0)
            lapTime = updatedTime - lastLapTime;

        laps.add(0, new Lap(lapTime));
        lastLapTime = updatedTime;
        adapter.notifyDataSetChanged();
        startTime = SystemClock.uptimeMillis();
        timeInMilliseconds = 0L;
        timeSwapBuffer = 0L;
        updatedTime = 0L;
        if (sendMessage)
            sendMessage(SAVE_LAP_MESSAGE, Long.toString(lapTime));
    }

    private void ResetStopWatch(boolean sendMessage) {
        lastLapTime = 0L;
        startTime = 0L;
        timeInMilliseconds = 0L;
        timeSwapBuffer = 0L;
        updatedTime = 0L;
        adapter.clear();
        refreshTimeText();
        if (sendMessage)
            sendMessage(RESET_STOPWATCH, null);
    }

    private void PauseStopWatch(long timeSwapBufferParam, boolean sendMessage) {

        if (timeSwapBufferParam == 0)
            timeSwapBuffer += timeInMilliseconds;
        else
            timeSwapBuffer = timeSwapBufferParam;

        handler.removeCallbacks(updater);

        ibStart.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_playback_play));
        ibStart.setBackgroundColor(getResources().getColor(R.color.green));
        ibStop.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_reload));
        ibStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
        running = false;

        if (sendMessage)
            sendMessage(PAUSE_WATCH, Long.toString(timeSwapBuffer));
    }

    private void StartStopWatch(long startTimeParam, boolean sendMessage) {
        if (startTimeParam == 0)
            startTime = SystemClock.uptimeMillis();
        else
            startTime = startTimeParam;

        handler.post(updater);

        // Start
        ibStart.setImageDrawable(getResources().getDrawable(
                R.mipmap.ic_action_playback_pause));
        ibStart.setBackgroundColor(getResources().getColor(R.color.orange));
        ibStop.setImageDrawable(getResources().getDrawable(R.mipmap.ic_action_add));
        ibStop.setBackgroundColor(getResources().getColor(R.color.blue));
        running = true;

        if (sendMessage)
            sendMessage(START_STOPWATCH_PATH, Long.toString(startTime));
    }

    private void sendMessage(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(googleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi
                            .sendMessage(googleApiClient, node.getId(), path,
                                    text != null ? text.getBytes() : null).await();
                }
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(getClass().getSimpleName(), "onConnected: " + connectionHint);
        Toast.makeText(MainActivity.this, "Connected to Data Layer API",
                Toast.LENGTH_SHORT).show();
        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(getClass().getSimpleName(), "onConnectionSuspended: " + cause);
        Toast.makeText(MainActivity.this, "Connectio to Data ayer API suspended",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getClass().getSimpleName(), "onConnectionFailed: " + connectionResult);
        Toast.makeText(MainActivity.this, "Connection to Data Layer API failed",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(messageEvent.getPath(), "message received");
        if (messageEvent.getPath().equals(START_STOPWATCH_PATH)) {
            startTime = Long.valueOf(new String(messageEvent.getData()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StartStopWatch(startTime, false);
                }
            });
        }
        else if (messageEvent.getPath().equals(PAUSE_WATCH)) {
            timeSwapBuffer = Long.valueOf(new String(messageEvent.getData()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PauseStopWatch(timeSwapBuffer, false);
                }
            });

        }
        else if (messageEvent.getPath().equals(RESET_STOPWATCH)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ResetStopWatch(false);
                }
            });
        }
        else if (messageEvent.getPath().equals(SAVE_LAP_MESSAGE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long lapTime = Long.valueOf(new String(messageEvent.getData()));
                    SaveLap(lapTime, false);
                }
            });
        }
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
