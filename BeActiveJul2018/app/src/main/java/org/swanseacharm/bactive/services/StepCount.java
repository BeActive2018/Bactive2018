package org.swanseacharm.bactive.services;

import android.app.AlarmManager;
import android.app.IntentService;

import android.app.Notification;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.swanseacharm.bactive.BuildConfig;
import org.swanseacharm.bactive.R;
import org.swanseacharm.bactive.receivers.ReceiverCall;
import org.swanseacharm.bactive.ui.MainActivity;

import java.util.logging.Logger;

public class StepCount extends IntentService implements SensorEventListener {
    private static long mstepsTakenTot=0;
    private static int mstepsTakenSince12AM=0;
    private int mLastStep;
    private String my_Action = "MY_ACTION";

    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HALF_HOUR;
    private final static int SAVE_OFFSET_STEPS = 500;

    private SensorManager mSensorManager;
    private Sensor mStepCounter;


    private int mStartMode = START_STICKY;
    private IBinder mBinder;
    private boolean mAllowRebind;

    public static int getMstepsTakensince12AM()
    {
        return mstepsTakenSince12AM;
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
    public void onSensorChanged(final SensorEvent e)
    {
            if(e.values[0]>Integer.MAX_VALUE)
            {
                return;
            }
            else
            {
                mstepsTakenSince12AM = (int) e.values[0];

            }

                Intent intent = new Intent();
                intent.setAction(String.valueOf(mstepsTakenSince12AM));
                intent.setAction(String.valueOf(mstepsTakenTot));

                intent.putExtra("DATA_STEPS_TODAY", mstepsTakenSince12AM);
                intent.putExtra("DATA_STEP_TOTAL", mstepsTakenTot);
                sendBroadcast(intent);


    }

    public boolean isCompatibleAndroid()
    {
        PackageManager pm = getPackageManager();
        //check if compatible
        return pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_SENSOR_STEP_COUNTER);
    }

    @Override
    public void onHandleIntent(Intent intent)
    {


    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Intent notificationIntent = new Intent(this, ReceiverCall.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Be Active!")
                .setContentText("Pedometer service")
                .setContentIntent(pendingIntent).build();
        startForeground(1337,notification);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mStepCounter= mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        Log.i("stepcount","onCreate called+++++++++++");

    }

    @Override
    public void onStart(Intent intent,int startId)
    {
        super.onStart(intent, startId);
        Intent intent1 = new Intent("android.intent.action.MAIN");

        intent.putExtra("STEPS_SO_FAR",mstepsTakenTot);
        intent.putExtra("STEPS_SINCE_12AM",mstepsTakenSince12AM);
        intent.putExtra("LAST_STEP",mLastStep);
        intent1.putExtra("TEST_BROADCAST", "Broadcast test 1");
        this.sendBroadcast(intent1);
        Log.i("stepcount","onStart called +++++++++++++++");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent,flags,startId);
        mSensorManager.registerListener(this,mStepCounter,SensorManager.SENSOR_DELAY_NORMAL);

        Log.i("stepcount","Compatible = "+isCompatibleAndroid());
        Log.i("stepcount","onStartCommand Called +++++++++++++");
        return mStartMode;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i("stepcount","onDestory called ++++++++++++++");

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
