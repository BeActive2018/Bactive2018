package org.swanseacharm.bactive.ui;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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


    private int mStepsPerCal = 20;//steps per calorie burned
    private double mMetersPerStep = 0.701;//distance per step (meters)

    private Intent mServiceIntent;
    private String tag = "MAINACTIVITY";
    private int todaySteps=0;

    private BroadcastReceiver receiver;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Check if service is running (should only occur on first time setup)
        StepCounter mSensorService = new StepCounter();
        mServiceIntent = new Intent(this.getApplicationContext(),mSensorService.getClass());
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


        Log.i("BACTIVE INFO:", "Bactive Activity app started");

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

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.bactivelogo);

        createNotificationChannel();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "Bactive")
                .setSmallIcon(R.drawable.bactivelogo_statusbar)
                .setOngoing(true)
                .setContentTitle("Bactive!")
                .setContentText("Keep those steps up!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, mBuilder.build());

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Bactive", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("org.swanseacharm.bactive.FRESHDATA");//receives step count data from StepCounter.class
        receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DecimalFormat df = new DecimalFormat("#.##");//format for distance travelled
            todaySteps = intent.getIntExtra("DATA_STEPS_TODAY",0);//get today's steps
            //set view objects to values
            binding.stepsTakenToday.setText(String.valueOf(todaySteps));
            binding.textView9.setText(String.valueOf(todaySteps/mStepsPerCal));
            binding.textView10.setText(String.valueOf(Double.valueOf(df.format((todaySteps*mMetersPerStep)/1000))));
            Log.d("Main activity", "broadcast recieved");
            updateGreenManPosition();//update position of green character
        }
    };
        this.registerReceiver(this.receiver,intentFilter);

        Intent intent = new Intent("org.swanseacharm.bactive.services")
                .putExtra("REQUEST_FRESH_DATA",true);
        sendBroadcast(intent);//request fresh step data



    }

    private void updateGreenManPosition()//update position of green man depending on steps compared to group average
    {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.imageView5).getLayoutParams();
        //TODO remove and replace with values from database
        int temporyDevInt =10000;
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
    public void onDestroy()
    {
        super.onDestroy();
        stopService(mServiceIntent);//stop StepCounter service so it doesn't die with the thread
        Log.d(tag,"Stopping service");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass)//checks if service is already running
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



//TODO get database information and put it into group average
}


