package com.example.user.heartratemonitor;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueTooth extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    Button btnOn, btnOff;
    TextView txtArduino, txtString, txtStringLength, sensorView0, sensorView1, sensorView2, sensorView3;
    Handler bluetoothIn;

    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;
    private UserActionThread userActionThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_combined);

        //Link the buttons and textViews to respective views
        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        txtString = (TextView) findViewById(R.id.txtString);
        txtStringLength = (TextView) findViewById(R.id.testView1);
        sensorView0 = (TextView) findViewById(R.id.sensorView0);
        sensorView1 = (TextView) findViewById(R.id.sensorView1);
        sensorView2 = (TextView) findViewById(R.id.sensorView2);
        sensorView3 = (TextView) findViewById(R.id.sensorView3);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = dataInPrint;          //get sensor value from string between indices 1-5
//                            String sensor1 = recDataString.substring(6, 10);            //same again...
//                            String sensor2 = recDataString.substring(11, 15);
//                            String sensor3 = recDataString.substring(16, 20);

                            sensorView0.setText(" Heart Rate " + sensor0 + "BPM");	//update the textviews with sensor values
//                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
//                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
//                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        //setContentView(R.layout.activity_combined);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.INTENT_FILTER));

        mClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mClient.connect();

        /*ActivityRecognitionClient activityRecognitionClient = ActivityRecognition.getClient(this);
        Task task = activityRecognitionClient.requestActivityUpdates(180_000L, pendingIntent);
*/

        mStartBtn = (Button)findViewById(R.id.startBtn);
        mStopBtn = (Button)findViewById(R.id.stopBtn);
        mCheckBtn = (Button)findViewById(R.id.checkBtn);
        mActivityType = (TextView)findViewById(R.id.activityTypes);
        mConfidenceLevel = (TextView)findViewById(R.id.confidence);
        mStatus = (TextView)findViewById(R.id.status);
        mPastActivities = (TextView)findViewById(R.id.pastActivities);


        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.disconnect();
                LocalBroadcastManager.getInstance(BlueTooth.this).unregisterReceiver(mBroadcastReceiver);
                mStatus.setText("Status: stopped");
                Toast.makeText(BlueTooth.this,"Stopped",Toast.LENGTH_SHORT).show();
                activityRecPoints.clear();
                //mActivityType.setText("--");
                //mConfidenceLevel.setText("--");
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.connect();
                LocalBroadcastManager.getInstance(BlueTooth.this).registerReceiver(mBroadcastReceiver,
                        new IntentFilter(Constants.INTENT_FILTER));

                Toast.makeText(BlueTooth.this,"Started",Toast.LENGTH_SHORT).show();
            }
        });

        mCheckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Toast.makeText(BlueTooth.this,"Checked",Toast.LENGTH_SHORT).show();
            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
            }
        });

        btnOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");    // Send "1" via Bluetooth
                Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();


        //userActionThread = new UserActionThread(btSocket);
       // userActionThread.start();
      //  userActionThread.write("x");

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
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

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    private class UserActionThread extends Thread {
        /*private final InputStream mmInStream;
        private final OutputStream mmOutStream;
*/
        //creation of the connect thread
        public UserActionThread(BluetoothSocket socket) {
//            InputStream tmpIn = null;
//            OutputStream tmpOut = null;
//
//            try {
//                //Create I/O streams for connection
//                tmpIn = socket.getInputStream();
//                tmpOut = socket.getOutputStream();
//            } catch (IOException e) { }
//
//            mmInStream = tmpIn;
//            mmOutStream = tmpOut;
        }


        public void run() {
            /*byte[] buffer = new byte[256];
            int bytes;*/

            // Keep looping to listen for received messages
            while (true) {

                    /*bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);*/
                    // Send the obtained bytes to the UI Activity via handler
                    //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
//                    Toast.makeText(getBaseContext(), "ok", Toast.LENGTH_LONG).show();

            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
        }
    }

    //bluetooto starts

    private List<ActivityRecPoint> activityRecPoints = new ArrayList<>();
    private Button mStartBtn;
    private Button mStopBtn;
    private Button mCheckBtn;
    private TextView mActivityType;
    private TextView mConfidenceLevel;
    private TextView mStatus;
    private TextView mPastActivities;

    private static final int REQUEST_CODE = 0;
    // every 3 minutes
    private static final long UPDATE_INTERVAL = 1000;
    private GoogleApiClient mClient;

    //private Intent intent;



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
        LocalBroadcastManager.getInstance(BlueTooth.this).unregisterReceiver(mBroadcastReceiver);
        Toast.makeText(BlueTooth.this,"Activity Recognition Stopped!",Toast.LENGTH_SHORT).show();
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
