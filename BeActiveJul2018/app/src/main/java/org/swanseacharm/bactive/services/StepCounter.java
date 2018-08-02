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

import java.util.Calendar;

public class StepCounter extends Service {

    private String lastSave="0";

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

    public StepCounter()
    {
        super();
        Log.d(tag,"Constructor called");
    }

    public void sendFreshData()//broadcasts step data
    {
        Intent intent = new Intent("org.swanseacharm.bactive.FRESHDATA")
                .putExtra("DATA_STEPS_TODAY",stepsSince12);
        Log.d(tag,"sendFreshData broadcast "+ stepsSince12);
        sendBroadcast(intent);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        lastSave="";

        //Receive if a boot has happened
        IntentFilter intentFilter = new IntentFilter("org.swanseacharm.bactive.services");
        mBootReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mBooted=intent.getBooleanExtra("DATA_FROM_BOOTED",false);
                if(mBooted)
                {
                    oldSteps=0;//reset old steps as STEP_COUNTER counts steps from boot
                }

            }
        };
        mMultiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {//receives requests
                Log.d(tag,"MultiReciever receiving");
                if(intent.getBooleanExtra("REQUEST_FRESH_DATA",false))//receive request for fresh data
                {
                    Log.d(tag,"Fresh data request made");
                    sendFreshData();//send step count
                }
            }
        };
        this.registerReceiver(mBootReciever,intentFilter);
        this.registerReceiver(mMultiReceiver,intentFilter);

        IntentFilter intentFilter2 = new IntentFilter("org.swanseacharm.bactive.SAVEDATA");
        mSaveDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {//on receive restart and reset step count
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
        //JobSchedule.scheduleJob(getApplicationContext()); //may not be needed
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent,flags,startId);
        //setup step sensor
        try {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            mSensorManager.registerListener(mSensorEventListener, mStepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        catch (NullPointerException e)
        {
            Log.e(tag,"NO SENSOR TYPE_STEP_COUNTER"+e.toString());
        }
        //get data if there is any e.g. ReceiverCall.class restarts service with old data
        if(intent==null)
        {
            intent = new Intent("filler");
        }
        if(!String.valueOf(intent.getIntExtra("DATA_STEPS_SINCE_TWELVE",0)).equals(""))
        {
            stepsSince12 = intent.getIntExtra("DATA_STEPS_SINCE_TWELVE",0);
        }

        lastSave = intent.getStringExtra("DATA_LAST_SAVE");
        if(lastSave==null||lastSave.equals(""))
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
        Log.d(tag,"onDestroy Called");
        //on destroy put data into intent and broadcast death of StepCounter.class
        Intent broadcastIntent = new Intent("org.swanseacharm.bactive.receivers")
                .putExtra("DATA_STEPS_SINCE_TWELVE",stepsSince12)
                .putExtra("DATA_OLD_STEPS",oldSteps)
                .putExtra("DATA_LAST_SAVE",lastSave);

        sendBroadcast(broadcastIntent); //broadcast to auto restart service

        this.unregisterReceiver(mBootReciever);
        this.unregisterReceiver(mMultiReceiver);
        this.unregisterReceiver(mSaveDataReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
