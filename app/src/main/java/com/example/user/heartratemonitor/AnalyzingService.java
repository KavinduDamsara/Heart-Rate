package com.example.user.heartratemonitor;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import java.util.Date;


public class AnalyzingService extends IntentService {
    private int restTimeAVG;
    private int workingAVG;
    private int varient;
    private String userState;  //1 for resting 2 for exercycing

    public AnalyzingService(String name) {
        super(name);
    }



    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
    public HeartRateResults analyze(int BPM){
        Log.e("AnalyzingService","analyzing started");
        getUserState();
        decideUserHRLevels();
        if(userState.equals("still") && (BPM < (restTimeAVG-varient) || BPM > (restTimeAVG+varient))){
            return new HeartRateResults(BPM, userState, new Date());
        }
        else {
            return null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);


    }
    public void decideUserHRLevels(){
        restTimeAVG = 72;
        workingAVG = 175;
        varient = 10;
    }
    public void getUserState(){
        userState = "still";
    }

}
