package com.cologne.hackaton.wearstopwatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gabriel.android.timelib.StopWatch;
import com.gabriel.android.timelib.model.Lap;
import com.gabriel.android.timelib.utils.StringUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmytro Khmelenko
 */
public class MainActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private static final String START_STOPWATCH_PATH = "start/stopwatch";
    private static final String SAVE_LAP_MESSAGE = "save/laptime";
    private static final String RESET_STOPWATCH = "reset/watch";
    private static final String PAUSE_WATCH = "pause/watch";

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

    // Wear Api
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                } else {
                    mLaps.clear();
                }
                mLapsAdapter.notifyDataSetChanged();
            }
        });

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
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

        } else {
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
        } else {
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
            mBtnStop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            mBtnStart.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            mBtnStop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            mLocked = true;
        } else {
            // Unlock
            mBtnLock.setImageDrawable(getResources().getDrawable(
                    R.mipmap.ic_action_lock_open));
            if (stopWatch.isRunning()) {
                mBtnStop.setBackgroundColor(getResources().getColor(R.color.dark_blue));
                mBtnStart.setBackgroundColor(getResources().getColor(R.color.orange));
            } else {
                mBtnStart.setBackgroundColor(getResources().getColor(R.color.green));
                mBtnStop.setBackgroundColor(getResources().getColor(R.color.blue));
            }
            mLocked = false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(getClass().getSimpleName(), "onConnected: " + bundle);
        Toast.makeText(MainActivity.this, "Connected to Data Layer API",
                Toast.LENGTH_SHORT).show();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(getClass().getSimpleName(), "onConnectionSuspended: " + i);
        Toast.makeText(MainActivity.this, "Connection to Data ayer API suspended",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(messageEvent.getPath(), "message received");
        if (messageEvent.getPath().equals(START_STOPWATCH_PATH)) {
//            mStartTime = Long.valueOf(new String(messageEvent.getData()));
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    startStopWatch(mStartTime, false);
//                }
//            });
        } else if (messageEvent.getPath().equals(PAUSE_WATCH)) {
            //mTimeSwapBuffer = Long.valueOf(new String(messageEvent.getData()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopWatch.pauseStopWatch();
                }
            });

        } else if (messageEvent.getPath().equals(RESET_STOPWATCH)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopWatch.resetStopWatch();

                    // clear all data
                    mLaps.clear();
                    mLapsAdapter.notifyDataSetChanged();
                }
            });
        } else if (messageEvent.getPath().equals(SAVE_LAP_MESSAGE)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String[] data = new String(messageEvent.getData()).split("#");
                    Lap lap = new Lap(Integer.valueOf(data[0]), Long.valueOf(data[1]), Long.valueOf(data[2]));
                    mLaps.add(0, lap);

                    mLapsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getClass().getSimpleName(), "onConnectionFailed: " + connectionResult);
        Toast.makeText(MainActivity.this, "Connection to Data Layer API failed",
                Toast.LENGTH_SHORT).show();
    }
}
