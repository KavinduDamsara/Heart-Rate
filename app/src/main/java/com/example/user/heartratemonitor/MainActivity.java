package com.example.user.heartratemonitor;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import  android.content.Intent;

public class MainActivity extends AppCompatActivity {

    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this,LogIn_Firebase.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }
    /*@Override
    public void onResume(){
        super.onResume();

        Intent myIntent = new Intent(getBaseContext(),   LogIn_Firebase.class);
        startActivity(myIntent);
    }*/

}
