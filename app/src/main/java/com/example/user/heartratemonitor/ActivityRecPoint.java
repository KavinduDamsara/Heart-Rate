package com.example.user.heartratemonitor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActivityRecPoint implements Serializable {
    private String activityType;
    private Long time;


    public ActivityRecPoint(String activityType, Long time) {
        this.activityType = activityType;
        this.time = time;
    }

    public ActivityRecPoint() {
        this.activityType = "UNKNOWN";
        this.time = 0L;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.UK);
        Date date = new Date(time);
        return "activityType='" + activityType + '\'' +
                ", time=" + formatter.format(date)+ "\n";
    }
}
