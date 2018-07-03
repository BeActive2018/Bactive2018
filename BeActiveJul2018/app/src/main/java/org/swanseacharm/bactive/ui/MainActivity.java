package org.swanseacharm.bactive.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.databinding.ActivityMainBinding;

import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private int mStepsPerCal = 20;
    private double mMetersPerStep = 0.701;

    private BroadcastReceiver receiver;

    private int mStepsToday=0;
    private long mTotSteps=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Log.i("BACTIVE INFO:", "onCreate called in mainActivity");

    }

    @Override
    public void onStart()
    {
        super.onStart();


        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
        this.registerReceiver(receiver,intentFilter);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int todaySteps = intent.getIntExtra("DATA_STEPS_TODAY",0);
                long totSteps = intent.getLongExtra("DATA_STEP_TOTAL",0);
                binding.stepsTakenToday.setText(todaySteps);
                binding.textView9.setText(todaySteps/mStepsPerCal);
                binding.textView10.setText(String.valueOf(todaySteps*mMetersPerStep));
                binding.textView6.setText(intent.getStringExtra("TEST_BROADCAST"));
                binding.textView4.setText("Broadcast recieved");
            }
        };

        startService(new Intent(this,org.swanseacharm.bactive.services.StepCount.class));

    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.main");
        this.registerReceiver(receiver,intentFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        this.unregisterReceiver(this.receiver);
    }

    @Override
    public void onStop()
    {
        super.onStop();

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }




}


