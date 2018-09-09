package com.example.user.heartratemonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import  android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @Override
    public void onResume(){
        super.onResume();

        Intent myIntent = new Intent(getBaseContext(),   LogIn_Firebase.class);
        startActivity(myIntent);
    }

}
