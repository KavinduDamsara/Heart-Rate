package com.example.user.heartratemonitor;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.List;

public class ActivityRecognizedService extends IntentService{

    private LocalBroadcastManager mLocalBroadcastManager;
    private List<ActivityRecPoint> activityRecPoints;

    @Override
    public void onCreate() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        activityRecPoints = new ArrayList<>();
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, startId, startId);
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        return START_STICKY;
    }
    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        logThis("onHandleIntent called...");
        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            handleDetectedActivities( result.getProbableActivities() );
        }
    }

    private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Log.e( "ActivityRecognition", "In Vehicle: " + activity.getConfidence() );
                    sendMessage("In Vehicle",activity.getConfidence());


                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e( "ActivityRecognition", "On Bicycle: " + activity.getConfidence() );
                    sendMessage("On Bicycle",activity.getConfidence());
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e( "ActivityRecognition", "On Foot: " + activity.getConfidence() );
                    sendMessage("On Foot",activity.getConfidence());
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e( "ActivityRecognition", "Running: " + activity.getConfidence() );
                    sendMessage("Running",activity.getConfidence());
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e( "ActivityRecognition", "Still: " + activity.getConfidence() );
                    sendMessage("Still",activity.getConfidence());
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e( "ActivityRecognition", "Tilting: " + activity.getConfidence() );
                    sendMessage("Tilting",activity.getConfidence());
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecognition", "Walking: " + activity.getConfidence() );
                    sendMessage("Walking",activity.getConfidence());
                    break;
                }

                case DetectedActivity.UNKNOWN: {
                    Log.e( "ActivityRecognition", "Unknown: " + activity.getConfidence() );
                    break;
                }
            }

        }


    }

    private void sendMessage(String message, int confidence) {
        Intent intent = new Intent(Constants.INTENT_FILTER);

        if(confidence >= 75) {
            ActivityRecPoint newPoint = new ActivityRecPoint(message,System.currentTimeMillis());


            intent.putExtra(Constants.LIST_ITEM_KEY, newPoint);

            intent.putExtra(Constants.MESSAGE_KEY, message);
            intent.putExtra(Constants.CONFIDENCE_KEY, confidence);
            mLocalBroadcastManager.sendBroadcast(intent);
        }

        logThis("List size:"+activityRecPoints.size());
    }

    private void logThis(String s){
        Log.d(ActivityRecognizedService.class.getSimpleName(),s);
    }
}
