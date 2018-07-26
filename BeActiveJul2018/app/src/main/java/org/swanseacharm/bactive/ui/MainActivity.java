package org.swanseacharm.bactive.ui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.databinding.ActivityMainBinding;
import org.swanseacharm.bactive.services.StepCounter;
import org.swanseacharm.bactive.Util;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;


    private int mStepsPerCal = 20;
    private double mMetersPerStep = 0.701;

    private Intent mServiceIntent;
    private Context ctx;
    private String tag = "MAINACTIVITY";
    private int todaySteps=0;

    public Context getCtx() {
        return ctx;
    }

    private BroadcastReceiver receiver;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        ctx = this;//set context
        //Check if service is running (should only occur on first time setup)
        StepCounter mSensorService = new StepCounter(getCtx());
        mServiceIntent = new Intent(getCtx(),mSensorService.getClass());
        if(!isMyServiceRunning(mSensorService.getClass()))
        {
            startService(mServiceIntent);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);//set binding
        //setting buttons
        Button yesterday;
        yesterday = (Button)findViewById(R.id.button5);
        yesterday.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,org.swanseacharm.bactive.ui.Yesterday.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
        Button history;
        history = (Button)findViewById(R.id.button7);
        history.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this,org.swanseacharm.bactive.ui.History.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });


        Log.i("BACTIVE INFO:", "onCreate called in mainActivity");

        //Setting application wide data
        SharedPreferences prefs = this.getSharedPreferences(
            "org.swanseacharm.bactive", Context.MODE_PRIVATE);
        Util util = new Util();

        prefs.edit().putInt("STEPS_PER_CAL",20).apply();
        util.putDouble(prefs.edit(),"METERS_PER_STEP",0.701).apply();

        //getting application wide data

        mStepsPerCal = prefs.getInt("STEPS_PER_CAL",20);
        mMetersPerStep = util.getDouble(prefs,"METERS_PER_STEP",0.701);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //start green man animation
        ImageView greenManAnimation = findViewById(R.id.imageView5);
        AnimationDrawable animation1 = (AnimationDrawable) greenManAnimation.getDrawable();
        animation1.start();
        //start group men animation
        ImageView groupAnimation = findViewById(R.id.imageView6);
        AnimationDrawable animation2 = (AnimationDrawable) groupAnimation.getDrawable();
        animation2.start();
        //start top background animation
        ImageView background = findViewById(R.id.imageView);
        AnimationDrawable animation3 = (AnimationDrawable) background.getDrawable();
        animation3.start();
        //start bottom background animation
        ImageView background2 = findViewById(R.id.imageView2);
        AnimationDrawable animation4 = (AnimationDrawable) background2.getDrawable();
        animation4.start();
        //update position of the green man
        updateGreenManPosition();

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
            todaySteps = intent.getIntExtra("DATA_STEPS_TODAY",0);
            long totSteps = intent.getLongExtra("DATA_STEP_TOTAL",0);
            binding.stepsTakenToday.setText(String.valueOf(todaySteps));
            binding.textView9.setText(String.valueOf(todaySteps/mStepsPerCal));
            binding.textView10.setText(String.valueOf(Double.valueOf(df.format((todaySteps*mMetersPerStep)/1000))));
            Log.i("Main activity", "broadcast recieved");
            updateGreenManPosition();
        }
    };
        this.registerReceiver(this.receiver,intentFilter);

        Intent intent = new Intent("org.swanseacharm.bactive.services")
                .putExtra("REQUEST_FRESH_DATA",true);
        sendBroadcast(intent);



    }

    private void updateGreenManPosition()
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.imageView5).getLayoutParams();
        //TODO remove and replace with values from database
        int temporyDevInt =1000;
        if(todaySteps<=temporyDevInt*0.05)
        {
            params.horizontalBias = 0.80f;
            binding.textView2.setText(R.string.bad);
        }
        else if(todaySteps<=temporyDevInt*0.25)
        {
            params.horizontalBias = 0.6f;
            binding.textView2.setText(R.string.ok);
        }
        else if(todaySteps<=temporyDevInt)
        {
            params.horizontalBias = 0.5f;
            binding.textView2.setText(R.string.good);
        }
        else if(todaySteps<=temporyDevInt*1.25)
        {
            params.horizontalBias = 0.4f;
            binding.textView2.setText(R.string.great);
        }
        else
        {
            params.horizontalBias = 0.2f;
            binding.textView2.setText(R.string.amazing);
        }
        findViewById(R.id.imageView5).setLayoutParams(params);



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


