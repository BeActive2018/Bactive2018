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

    private Receiver receiver;

    private int mStepsToday;
    private long mTotSteps;



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

        receiver=new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(String.valueOf(org.swanseacharm.bactive.services.StepCount.getMstepsTakensince12AM()));
        intentFilter.addAction(String.valueOf(org.swanseacharm.bactive.services.StepCount.getMstepsTakenTot()));
        registerReceiver(receiver,intentFilter);


    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        unregisterReceiver(receiver);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    private class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg1, Intent arg2)
        {

            int todaySteps = arg2.getIntExtra("DATA_STEPS_TODAY",0);
            long totSteps = arg2.getLongExtra("DATA_STEP_TOTAL",0);
            binding.stepsTakenToday.setText(todaySteps);
            binding.textView9.setText(todaySteps/mStepsPerCal);
            binding.textView10.setText(String.valueOf(todaySteps*mMetersPerStep));

        }
    }

}


