package com.gabriel.android.timelib.model;

/**
 * Represents lap data
 *
 * @author Dmytro Khmelenko, Gabriel Schlatter
 */
public class Lap {
    private int lapNumber;
    private final long lapTime;
    private final long timeSum;

    public Lap(int lapNumber, long lapTime, long timeSum) {
        this.lapNumber = lapNumber;
        this.lapTime = lapTime;
        this.timeSum = timeSum;
    }

    public long getLapTime() {
        return lapTime;
    }

    public long getTimeSum() {
        return timeSum;
    }

    public int getLapNumber() {
        return lapNumber;
    }
}