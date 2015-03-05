package com.cologne.hackaton.wearstopwatch.activity.event;

import com.gabriel.android.timelib.model.Lap;

import java.util.List;

/**
 * Created by admin on 3/5/2015.
 */
public class LapUpdateEvent {

  private List<Lap> laps;

  public LapUpdateEvent(List<Lap> laps) {
    this.laps = laps;
  }

  public List<Lap> getLaps() {
    return laps;
  }
}
