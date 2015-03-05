package com.cologne.hackaton.wearstopwatch.activity.stopwatch;

import com.gabriel.android.timelib.model.Lap;

import java.util.Comparator;

/**
 * Created by admin on 3/5/2015.
 */
public class LapComparator implements Comparator<Lap> {
  @Override
  public int compare(Lap l1, Lap l2) {
    // descending order (ascending order would be:
    // o1.getGrade()-o2.getGrade())
    return (l1.getLapNumber() < l2.getLapNumber() ? 1 : -1);
  }
}
