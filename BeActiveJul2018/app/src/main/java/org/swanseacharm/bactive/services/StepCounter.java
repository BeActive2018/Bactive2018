package org.swanseacharm.bactive.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TimeUtils;

import org.swanseacharm.bactive.Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class StepCounter extends Service {

    private String fileName="stepHistory.stp";
    private String saveFileName="savedData.stp";

    private String lastSave="0";
    private long saveInterval=60000/*3600000*/;//1hour in miliseconds

    private int stepsSince12=0;
    private int oldSteps=0;

    private boolean mBooted;

    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {//called when sensor has new data
            stepsSince12 += sensorEvent.values[0]-oldSteps;//set the steps to new value
            oldSteps=(int)sensorEvent.values[0];
            //Log.i(tag, "onSensorChanged");
            //Log.d(tag,"event value = "+sensorEvent.values[0]);
            //sendFreshData();//broadcast new data for rest of application

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //a magic thing that does nothing to this sensor
        }
    };

    private BroadcastReceiver mBootReciever;
    private BroadcastReceiver mMultiReceiver;
    private BroadcastReceiver mSaveDataReceiver;

    private String tag = "Pedometer service";

    public StepCounter(Context appcontext)
    {
        super();
        Log.i(tag,"Constructor called");
    }
    public StepCounter() {

    }

    public void sendFreshData()//broadcasts step data
    {

        Intent intent = new Intent("org.swanseacharm.bactive.FRESHDATA")
                .putExtra("DATA_STEPS_TODAY",stepsSince12);
        Log.i(tag,"sendFreshData broadcast "+ stepsSince12);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        //Recieve if a boot has happened
        IntentFilter intentFilter = new IntentFilter("org.swanseacharm.bactive.services");
        mBootReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mBooted=intent.getBooleanExtra("DATA_FROM_BOOTED",false);
                if(mBooted)
                {
                    oldSteps=0;
                }

            }
        };
        mMultiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(tag,"MultiReciever receiving");
                if(intent.getBooleanExtra("REQUEST_FRESH_DATA",false))//receive request for fresh data
                {
                    Log.i(tag,"Fresh data request made");
                    sendFreshData();
                }
                if(intent.getBooleanExtra("REQUEST_GET_HISTORY",false))//receive request for history file
                {
                    Log.i(tag,"Sending history");
                    sendBroadcast(new Intent("org.swanseacharm.bactive.ui").putExtra("DATA_HISTORY",getfile(getBaseContext())));
                }

            }
        };
        this.registerReceiver(mBootReciever,intentFilter);
        this.registerReceiver(mMultiReceiver,intentFilter);
        IntentFilter intentFilter2 = new IntentFilter("org.swanseacharm.bactive.SAVEDATA");
        mSaveDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                lastSave = String.valueOf(calendar.getTimeInMillis());
                if(intent.getBooleanExtra("REQUEST_FRESH_DATA",false))
                {
                    sendFreshData();
                }
                if(intent.getBooleanExtra("COMMAND_RESTART_SERVICE",false))
                {
                    stepsSince12 = 0;

                    sendFreshData();
                }
            }
        };
        this.registerReceiver(mSaveDataReceiver,intentFilter2);
        JobSchedule.scheduleJob(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent,flags,startId);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(mSensorEventListener,mStepSensor,SensorManager.SENSOR_DELAY_NORMAL);
        if(intent==null)
        {
            intent = new Intent("filler");
        }
        if(!String.valueOf(intent.getIntExtra("DATA_STEPS_SINCE_TWELVE",0)).equals(""))
        {
            stepsSince12 = intent.getIntExtra("DATA_STEPS_SINCE_TWELVE",0);
        }

        lastSave = intent.getStringExtra("DATA_LAST_SAVE");
        if(lastSave == "")
        {
            lastSave = "00000000";
        }
        if(!mBooted)
        {
            oldSteps = intent.getIntExtra("DATA_OLD_STEPS",0);
        }
        else
        {
            oldSteps=0;
            mBooted=false;
        }
        sendFreshData();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(tag,"onDestroy Called");
        Intent broadcastIntent = new Intent("org.swanseacharm.bactive.receivers")
                .putExtra("DATA_STEPS_SINCE_TWELVE",stepsSince12)
                .putExtra("DATA_OLD_STEPS",oldSteps)
                .putExtra("DATA_LAST_SAVE",lastSave);

        sendBroadcast(broadcastIntent); //broadcast to auto restart service

        this.unregisterReceiver(mBootReciever);
        this.unregisterReceiver(mMultiReceiver);
        this.unregisterReceiver(mSaveDataReceiver);
    }

    public String getfile(Context context)
    {
        Util util = new Util();
        return util.getfile(context);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
