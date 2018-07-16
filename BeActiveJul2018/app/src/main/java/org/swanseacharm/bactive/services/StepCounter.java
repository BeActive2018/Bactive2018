package org.swanseacharm.bactive.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

import org.swanseacharm.bactive.receivers.AlarmReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class StepCounter extends Service {

    private String fileName="stepHistory.stp";

    private String lastSave="0";
    private long saveInterval=60000/*3600000*/;//1hour in miliseconds

    private int stepsSince12=0;
    private int oldSteps=0;

    private boolean mBooted;


    private Timer timer;
    private TimerTask timerTask;


    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            stepsSince12 += sensorEvent.values[0]-oldSteps;
            oldSteps=(int)sensorEvent.values[0];
            Log.i(tag, "onSensorChanged");
            Log.d(tag,"event value = "+sensorEvent.values[0]);
            sendFreshData();

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //a magic thing that does nothing to this sensor
        }
    };

    private BroadcastReceiver mBootReciever;
    private BroadcastReceiver mMultiReceiver;

    private String tag = "Pedometer service";

    public StepCounter(Context appcontext)
    {
        super();
        Log.i(tag,"Constructor called");
    }
    public StepCounter() {

    }

    public void sendFreshData()
    {
        Intent intent = new Intent("org.swanseacharm.bactive.ui")
                .putExtra("DATA_STEPS_TODAY",stepsSince12);
        Log.i(tag,"sendFreshData broadcast "+ stepsSince12);
        Intent intent2 = new Intent("SaveDataService")
                .putExtra("DATA_STEPS_TODAY",stepsSince12);

        Log.d(tag,"old steps = "+oldSteps);
        sendBroadcast(intent);
        sendBroadcast(intent2);
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
                if(intent.getBooleanExtra("REQUEST_FRESH_DATA",false))
                {
                    Log.i(tag,"Fresh data request made");
                    sendFreshData();
                }
                if(intent.getBooleanExtra("REQUEST_GET_HISTORY",false))
                {
                    Log.i(tag,"Sending history");
                    sendBroadcast(new Intent("org.swanseacharm.bactive.ui").putExtra("DATA_HISTORY",getfile(getBaseContext())));
                }
                if(intent.getBooleanExtra("REQUEST_SAVE_DATA",false))
                {
                    //updateDecide();
                    updateFile();
                    Log.i(tag,"Saving data to file");
                }
            }
        };
        this.registerReceiver(mBootReciever,intentFilter);
        this.registerReceiver(mMultiReceiver,intentFilter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent,flags,startId);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(mSensorEventListener,mStepSensor,SensorManager.SENSOR_DELAY_NORMAL);
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

        startTask();
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

        sendBroadcast(broadcastIntent);
        this.unregisterReceiver(mBootReciever);
        this.unregisterReceiver(mMultiReceiver);
    }

    public void startTask()
    {
        /*Intent intent = new Intent(this,org.swanseacharm.bactive.services.StepCounter.class).putExtra("REQUEST_SAVE_DATA",true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(),234324243,intent,0);*/

        //Intent intent =  new Intent(StepCounter.this,AlarmReceiver.class);
        Intent intent = new Intent("org.swanseacharm.bactive.receivers");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(StepCounter.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GB"), Locale.UK);
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 6);
        //calendar.add(Calendar.DATE,+1);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),alarmManager.INTERVAL_DAY,pendingIntent);
        Log.i(tag,"Alarm made for "+calendar.getTime()+" ");
    }

    public void updateDecide()
    {
        Log.i(tag,"Save decide");

        SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        Log.i(tag,"Data last save ="+lastSave);
        Log.i(tag,"Data current date ="+Integer.parseInt(formatterDate.format(date)));
        if(lastSave == null)
        {
            lastSave = "00000000";
        }
        if(Integer.parseInt(lastSave)
                <Integer.parseInt(formatterDate.format(date)))
        {
            updateFile();
        }
    }




    public String getfile(Context context)
    {
        String ret = "";
        try{
            InputStream inputStream = context.openFileInput(fileName);

            if(inputStream!=null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String recieveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((recieveString = bufferedReader.readLine())!=null)
                {
                    stringBuilder.append(recieveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch(FileNotFoundException e){
            Log.e(tag, e.toString());
        }
        catch (IOException e){
            Log.e(tag, e.toString());
        }

        return ret;
    }

    public void updateFile()
    {
            try{
                SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
                SimpleDateFormat formatterTime = new SimpleDateFormat("HHMMSS");
                Date date = new Date();

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getBaseContext().openFileOutput(fileName,Context.MODE_APPEND));
                outputStreamWriter.write(formatterDate.format(date)+","+stepsSince12+"\n");
                lastSave = formatterDate.format(date);
                Log.i(tag,"sent data to file for long storage");

                stepsSince12 = 0;
                oldSteps = 0;
                Log.i(tag,"New day: reseting steps");
                sendFreshData();
            }
            catch (IOException e){
                Log.e("EXCEPTION", e.toString());
            }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(mSensorEventListener,mStepSensor,SensorManager.SENSOR_DELAY_NORMAL);
        Log.i(tag,"File update");
        }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
