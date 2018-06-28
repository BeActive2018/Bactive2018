package org.swanseacharm.bactive.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import org.swanseacharm.bactive.R;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Receiver receiver;

    private int mStepsToday;
    private long mTotSteps;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


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
            mStepsToday=todaySteps;
            mTotSteps=totSteps;
        }
    }

}


