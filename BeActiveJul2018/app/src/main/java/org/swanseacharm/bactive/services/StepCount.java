package org.swanseacharm.bactive.services;

import android.app.AlarmManager;
import android.app.IntentService;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

public class StepCount extends IntentService implements SensorEventListener {
    private static long mstepsTakenTot;
    private static int mstepsTakensince12AM;
    private int mLastStep;

    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HALF_HOUR;
    private final static int SAVE_OFFSET_STEPS = 500;

    private SensorManager mSensorManager;
    private Sensor mStepCounter;

    private int mStartMode = START_STICKY;
    private IBinder mBinder;
    private boolean mAllowRebind;

    public static int getMstepsTakensince12AM()
    {
        return mstepsTakensince12AM;
    }
    public static long getMstepsTakenTot()
    {
        return mstepsTakenTot;
    }

    public StepCount() {
        super("StepCount");
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor,int accuracy)
    {

    }

    @Override
    public void onSensorChanged(SensorEvent e)
    {
            if(mLastStep<1)
            {
                mLastStep=(int)e.values[0];
            }
            mstepsTakensince12AM = (int)e.values[0]-mLastStep;

                Intent intent = new Intent();
                intent.setAction(String.valueOf(mstepsTakensince12AM));
                intent.setAction(String.valueOf(mstepsTakenTot));

                intent.putExtra("DATA_STEPS_TODAY", mstepsTakensince12AM);
                intent.putExtra("DATA_STEP_TOTAL", mstepsTakenTot);
                sendBroadcast(intent);


    }

    public static boolean isCompatibleAndroid(PackageManager pm)
    {
        //min version Android KitKat
        int currentAPIVersion = (int) Build.VERSION.SDK_INT;

        //check if compatible
        return currentAPIVersion >=19 && pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_SENSOR_STEP_COUNTER);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        mSensorManager.registerListener(this,mStepCounter,SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onCreate()
    {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mStepCounter= mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        return mStartMode;
    }

    @Override
    public void onDestroy()
    {

        Intent intent = new Intent("org.swanseacharm.bactive");
        intent.putExtra("STEPS_SO_FAR",mstepsTakenTot);
        intent.putExtra("STEPS_SINCE_12AM",mstepsTakensince12AM);
        intent.putExtra("LAST_STEP",mLastStep);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent)
    {

    }

}
