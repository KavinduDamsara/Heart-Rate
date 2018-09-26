package com.example.user.heartratemonitor;

import android.location.Location;

import java.util.Date;

public class HeartRateResults {

    private int BPM;
    private String userState;
    private int estimatedRate;
    private Date time;
    private Location location;
    public HeartRateResults() {
    }

    public HeartRateResults(int BPM, String userState, int estimatedRate, Date time, Location location) {
        this.BPM = BPM;
        this.userState = userState;
        this.estimatedRate = estimatedRate;
        this.time = time;
        this.location = location;
    }

    public HeartRateResults(int BPM, String userState, Date time) {
        this.BPM = BPM;
        this.userState = userState;
        this.time = time;
    }
}
