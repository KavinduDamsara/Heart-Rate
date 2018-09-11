package com.example.user.heartratemonitor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.util.ArrayList;
import java.util.List;

//////////


public class UserAction extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private List<ActivityRecPoint> activityRecPoints = new ArrayList<>();
    private Button mStartBtn;
    private Button mStopBtn;
    private TextView mActivityType;
    private TextView mConfidenceLevel;
    private TextView mStatus;
    private TextView mPastActivities;

    private static final int REQUEST_CODE = 0;
    // every 3 minutes
    private static final long UPDATE_INTERVAL = 1000;
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_action);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.INTENT_FILTER));

        mClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mClient.connect();


        mStartBtn = (Button)findViewById(R.id.startBtn);
        mStopBtn = (Button)findViewById(R.id.stopBtn);
        mActivityType = (TextView)findViewById(R.id.activityTypes);
        mConfidenceLevel = (TextView)findViewById(R.id.confidence);
        mStatus = (TextView)findViewById(R.id.status);
        mPastActivities = (TextView)findViewById(R.id.pastActivities);


        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.disconnect();
                LocalBroadcastManager.getInstance(UserAction.this).unregisterReceiver(mBroadcastReceiver);
                mStatus.setText("Status: stopped");
                Toast.makeText(UserAction.this,"Stopped",Toast.LENGTH_SHORT).show();
                activityRecPoints.clear();
                //mActivityType.setText("--");
                //mConfidenceLevel.setText("--");
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.connect();
                LocalBroadcastManager.getInstance(UserAction.this).registerReceiver(mBroadcastReceiver,
                        new IntentFilter(Constants.INTENT_FILTER));

                Toast.makeText(UserAction.this,"Started",Toast.LENGTH_SHORT).show();
            }
        });

    }

//    @Override
//    protected void onResume() {
//        Toast.makeText(MainActivity.this,"Started",Toast.LENGTH_SHORT).show();
//        super.onResume();
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logThis("OnConnected");
        Intent intent = new Intent(this,ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient,UPDATE_INTERVAL, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        logThis("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logThis("onConnectionFailed");
    }

    private void logThis(String s){
        Log.d(MainActivity.class.getSimpleName(),s);
    }


    @Override
    protected void onDestroy() {
        mClient.disconnect();
        LocalBroadcastManager.getInstance(UserAction.this).unregisterReceiver(mBroadcastReceiver);
        Toast.makeText(UserAction.this,"Activity Recognition Stopped!",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.MESSAGE_KEY);
            mActivityType.setText(message);
            mConfidenceLevel.setText("" + intent.getIntExtra(Constants.CONFIDENCE_KEY,0));
            mStatus.setText("Status: Receiving updates");

            ActivityRecPoint receivedPoint = (ActivityRecPoint) intent.getSerializableExtra(Constants.LIST_ITEM_KEY);
            activityRecPoints.add(receivedPoint);

            mPastActivities.setText(activityRecPoints.toString());
            logThis("Got message: " + message);
            logThis("List size:"+activityRecPoints.size());
        }
    };
}
