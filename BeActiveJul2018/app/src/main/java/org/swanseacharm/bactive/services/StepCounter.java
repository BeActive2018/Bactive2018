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

import java.util.Timer;
import java.util.TimerTask;

public class StepCounter extends Service {

    private int stepsSince12=0;
    private int oldSteps=0;

    private boolean mBooted;

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            stepsSince12 += sensorEvent.values[0]-oldSteps;
            oldSteps=(int)sensorEvent.values[0];
            Intent intent = new Intent("org.swanseacharm.bactive.ui")
                    .putExtra("DATA_STEPS_TODAY",stepsSince12);
            Log.i(tag,"onSensorChanged broadcast "+ stepsSince12);
            Log.d(tag,"event value = "+sensorEvent.values[0]);
            Log.d(tag,"old steps = "+oldSteps);
            sendBroadcast(intent);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //a magic thing that does nothing to this sensor
        }
    };

    private BroadcastReceiver mBootReciever;

    private String tag = "Pedometer service";

    public StepCounter(Context appcontext)
    {
        super();
        Log.i(tag,"Constructor called");
    }
    public StepCounter() {

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
        this.registerReceiver(mBootReciever,intentFilter);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent,flags,startId);
        mSensorManager.registerListener(mSensorEventListener,mStepSensor,SensorManager.SENSOR_DELAY_NORMAL);
        stepsSince12 = intent.getIntExtra("DATA_STEPS_SINCE_TWELVE",0);
        if(!mBooted)
        {
            oldSteps = intent.getIntExtra("DATA_OLD_STEPS",0);
        }
        else
        {
            oldSteps=0;
            mBooted=false;
        }

        //startTask();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(tag,"onDestroy Called");
        Intent broadcastIntent = new Intent("org.swanseacharm.bactive.recievers")
                .putExtra("DATA_STEPS_SINCE_TWELVE",stepsSince12)
                .putExtra("DATA_OLD_STEPS",oldSteps);
        sendBroadcast(broadcastIntent);
        //stopTask();
        this.unregisterReceiver(mBootReciever);
    }

    public void startTask()
    {



        /*timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, 1000,1000);*/
    }

    /*public void initializeTimerTask()
    {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(tag+" timer","keeping alive");
            }
        };
    }*/

    public void stopTask()
    {
        /*if (timer!=null)
        {
            timer.cancel();
            timer=null;
        }*/

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
