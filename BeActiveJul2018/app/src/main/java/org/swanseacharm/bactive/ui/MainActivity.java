package org.swanseacharm.bactive.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.databinding.ActivityMainBinding;
import org.swanseacharm.bactive.services.StepCounter;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    private int mStepsPerCal = 20;
    private double mMetersPerStep = 0.701;
    private Intent mServiceIntent;
    private StepCounter mSensorService;
    private Context ctx;
    private String tag = "MAINACTIVITY";

    public Context getCtx() {
        return ctx;
    }

    private BroadcastReceiver receiver;
    private Button yesterday;
    private Button history;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        mSensorService = new StepCounter(getCtx());
        mServiceIntent = new Intent(getCtx(),mSensorService.getClass());

        if(!isMyServiceRunning(mSensorService.getClass()))
        {
            startService(mServiceIntent);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        yesterday = (Button)findViewById(R.id.button5);
        yesterday.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,org.swanseacharm.bactive.ui.Yesterday.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        history = (Button)findViewById(R.id.button7);
        history.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,org.swanseacharm.bactive.ui.History.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        Log.i("BACTIVE INFO:", "onCreate called in mainActivity");

    }

    @Override
    public void onStart()
    {
        super.onStart();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("org.swanseacharm.bactive.FRESHDATA");
        receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DecimalFormat df = new DecimalFormat("#.##");
            int todaySteps = intent.getIntExtra("DATA_STEPS_TODAY",0);
            long totSteps = intent.getLongExtra("DATA_STEP_TOTAL",0);
            binding.stepsTakenToday.setText(String.valueOf(todaySteps));
            binding.textView9.setText(String.valueOf(todaySteps/mStepsPerCal));
            binding.textView10.setText(String.valueOf(Double.valueOf(df.format((todaySteps*mMetersPerStep)/1000))));
            Log.i("Main activity", "broadcast recieved");
        }
    };
        this.registerReceiver(this.receiver,intentFilter);

        Intent intent = new Intent("org.swanseacharm.bactive.services")
                .putExtra("REQUEST_FRESH_DATA",true);
        sendBroadcast(intent);


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
        stopService(mServiceIntent);
        Log.i(tag,"Stopping service");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if(serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }




}


