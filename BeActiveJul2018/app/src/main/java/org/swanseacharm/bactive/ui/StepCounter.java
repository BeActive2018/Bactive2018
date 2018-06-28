package org.swanseacharm.bactive.ui;

import org.swanseacharm.bactive.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.hardware.SensorEventListener;

import java.io.Console;



public class StepCounter extends Activity implements SensorEventListener {

    private int stepCounter = 0;
    private int counterSteps = 0;
    private static boolean isRunning=false;
    private TextView mTextView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null)
        {
            stepCounter=savedInstanceState.getInt("STEP_COUNTER",0);
            counterSteps=savedInstanceState.getInt("COUNTER_STEP",0);
        }

        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text_view);
    }

    public void onStart()
    {
        super.onStart();
        if (isRunning ||!isCompatibleAndroid(getPackageManager()))
            return;
        isRunning=true;

    }

    public void onResume ()
    {
        super.onResume();
        mSensorManager.registerListener(this,mStepCounter,SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    public void onStop()
    {
        super.onStop();
    }

    public void onDestroy()
    {
        super.onDestroy();
    }

    /*void RegisterListeners(SensorType sensorType) {


        var sensorManager = (SensorManager) GetSystemService(Context.SensorService);
        var sensor = sensorManager.GetDefaultSensor(sensorType);

        sensorManager.RegisterListener(this, sensor, SensorDelay.Normal);
        Console.WriteLine("Sensor listener registered of type: " + sensorType);
    }*/

    public static boolean isCompatibleAndroid(PackageManager pm)
    {
        //min version Android KitKat
        int currentAPIVersion = (int) Build.VERSION.SDK_INT;

        //check if compatible
        return currentAPIVersion >=19 && pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_SENSOR_STEP_COUNTER);
    }

    public void onSensorChanged (SensorEvent e)
    {
        if (counterSteps<1)
        {
            counterSteps=(int)e.values[0];
        }

        stepCounter = (int)e.values [0] - counterSteps;

    }

    public void onAccuracyChanged(Sensor sensor,int accuracy)
    {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mTextView.setText(savedInstanceState.getString("TEXT_VIEW_KEY"));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("STEP_COUNTER", stepCounter);
        outState.putInt("COUNTER_STEP", counterSteps);
        outState.putString("TEXT_VIEW_KEY", mTextView.getText());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }



}
