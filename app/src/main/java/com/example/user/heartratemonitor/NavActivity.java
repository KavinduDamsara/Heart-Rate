package com.example.user.heartratemonitor;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private FirebaseAuth mAuth;
    private static final String TAG = "";

    //from combined
    TextView txtString, txtStringLength, heartRate, results;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private NavActivity.ConnectedThread mConnectedThread;
    //private NavActivity.UserActionThread userActionThread;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC address
    private static String address;
    //from combined
    private AnalyzingService analyzingSer = new AnalyzingService("analyzingSer");

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        txtString = (TextView) findViewById(R.id.txtString);
        txtStringLength = (TextView) findViewById(R.id.testView1);
        heartRate = (TextView) findViewById(R.id.heartRate);
        activityTypeImg = (ImageView) findViewById(R.id.activityTypeImg);
        results = (TextView) findViewById(R.id.results);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("~");
                    if (endOfLineIndex > 0) {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        if (recDataString.charAt(0) == '#')								//if it starts with # they are gathered
                        {
                            String BPM = dataInPrint.substring(1);
                            heartRate.setText( BPM + "BPM");
                        }
                        HeartRateResults BPMres = analyzingSer.analyze((int)Float.parseFloat(dataInPrint.substring(1)));
                        if(BPMres != null){
                            results.setText("At risk");
                        }
                        else {
                            results.setText("No risk");
                        }

                        recDataString.delete(0, recDataString.length()); 					//clear all

                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.INTENT_FILTER));

        mClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mClient.connect();
        mActivityType = (TextView)findViewById(R.id.activityTypes);
        mConfidenceLevel = (TextView)findViewById(R.id.confidence);
        mPastActivities = (TextView)findViewById(R.id.pastActivities);

        //main view components
        activityTypeImg = (ImageView)findViewById(R.id.activityTypeImg);
        //main view components end

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_signout) {
            FirebaseUser user = mAuth.getCurrentUser();
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(),LogIn_Firebase.class));
            return true;
        }
        if (id == R.id.action_verifyemail) {
            sendEmailVerification();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            startActivity(new Intent(getApplicationContext(),DeviceListActivity.class));
            Toast.makeText(NavActivity.this,"Checked",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_gallery) {
            mClient.connect();
            LocalBroadcastManager.getInstance(NavActivity.this).registerReceiver(mBroadcastReceiver,
                    new IntentFilter(Constants.INTENT_FILTER));

            Toast.makeText(NavActivity.this,"Started",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_user_details) {
            startActivity(new Intent(getApplicationContext(),UpdateUserProfile.class));
            Toast.makeText(NavActivity.this,"Add user details",Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void sendEmailVerification() {
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(NavActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(NavActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }


    @Override
    public void onResume() {
        super.onResume();


        //Getting MAC address
        Intent intent = getIntent();
        if(intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS) != null){
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {

            }
        }
        mConnectedThread = new NavActivity.ConnectedThread(btSocket);
        mConnectedThread.start();
        mConnectedThread.write("x");
        }
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if(btSocket != null){
        try
        {
            btSocket.close();
        } catch (IOException e2) {

        }
        }
    }

    //Checks that the phone's Bluetooth is available and if it os switch on
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {

                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    //bluetooto starts

    private List<ActivityRecPoint> activityRecPoints = new ArrayList<>();

    private TextView mActivityType;
    private TextView mConfidenceLevel;
    private TextView mPastActivities;

    private static final int REQUEST_CODE = 0;
    private static final long UPDATE_INTERVAL = 1000;
    private GoogleApiClient mClient;

    //main view components
    private ImageView activityTypeImg;
    //main view components end

    @Override
    protected void onStart() {
        super.onStart();
        Intent mIntent = new Intent(this, AnalyzingService.class);
        bindService(mIntent, analyzeService, BIND_AUTO_CREATE);
    }

    ServiceConnection analyzeService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(NavActivity.this, "Service is disconnected", 1000).show();

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(NavActivity.this, "Service is connected", 1000).show();
        }
    };

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
        LocalBroadcastManager.getInstance(NavActivity.this).unregisterReceiver(mBroadcastReceiver);
        Toast.makeText(NavActivity.this,"Activity Recognition Stopped!",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.MESSAGE_KEY);
            mActivityType.setText(message);
            mConfidenceLevel.setText("" + intent.getIntExtra(Constants.CONFIDENCE_KEY,0));
            setActivity(message);

            ActivityRecPoint receivedPoint = (ActivityRecPoint) intent.getSerializableExtra(Constants.LIST_ITEM_KEY);
            activityRecPoints.add(receivedPoint);

            mPastActivities.setText(activityRecPoints.toString());
            logThis("Got message: " + message);
            logThis("List size:"+activityRecPoints.size());
        }
    };

    public void setActivity(String message){

        if(message.equals("Running")){
            activityTypeImg.setImageResource(R.drawable.running);
        }
        else if(message.equals("In Vehicle")){
            activityTypeImg.setImageResource(R.drawable.vehicle);
        }
        else if(message.equals("On Bicycle")){
            activityTypeImg.setImageResource(R.drawable.on_bicycle);
        }
        else if(message.equals("On Foot")){
            activityTypeImg.setImageResource(R.drawable.on_foot);
        }
        else if(message.equals("Still")){
            activityTypeImg.setImageResource(R.drawable.still);
        }
        else if(message.equals("Tilting")){
            activityTypeImg.setImageResource(R.drawable.tilting);
        }
        else if(message.equals("Walking")){
            activityTypeImg.setImageResource(R.drawable.on_foot);
        }
        else {
            activityTypeImg.setImageResource(R.drawable.loading);
        }



    }

}
