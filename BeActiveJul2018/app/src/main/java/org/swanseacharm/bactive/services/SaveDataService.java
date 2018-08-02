package org.swanseacharm.bactive.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SaveDataService extends Service {

    private int stepsSince12;//holder for steps taken today

    private String tag="SaveDataService";
    private String fileName="stepHistory.stp";//file name

    private BroadcastReceiver receiver;

    public SaveDataService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(tag,"onCreate called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag,"onStartCommand called");
        IntentFilter intentFilter = new IntentFilter("org.swanseacharm.bactive.FRESHDATA");//intent filter for receiving fresh step count
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {//on receive of fresh step data
                stepsSince12 = intent.getIntExtra("DATA_STEPS_TODAY",0);//set step holder to StepCounters number of steps from StepCounter.class
                updateFile();//save data
                Log.d(tag,"Fresh data received");
                stopSelf();//task complete stop service

            }
        };
        this.registerReceiver(receiver,intentFilter);

        Intent sendIntent = new Intent("org.swanseacharm.bactive.SAVEDATA")//intent to tell StepCounter.class to restart and reset step counter.
                .putExtra("REQUEST_FRESH_DATA",true)
                .putExtra("COMMAND_RESTART_SERVICE",true);
        sendBroadcast(sendIntent);
        Log.d(tag,"Fresh Data request + RESTART command");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void updateFile()
    {
        try{
            SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);//date format for saving data. this format allows easy parsing e.g. date1<date2
            //set calender to yesterday (it kept putting it a day later then it should have so I -1'd the date used)
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DATE,-1);
            //append to output file
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getBaseContext().openFileOutput(fileName,Context.MODE_APPEND));
            outputStreamWriter.write(stepsSince12+","+formatterDate.format(calendar.getTimeInMillis())+"/n");//format = "STEP_COUNT,YYYYMMDD"
            outputStreamWriter.close();

            Log.d(tag,stepsSince12+","+formatterDate.format(calendar.getTimeInMillis())+"/n");
            Log.i(tag,"sent data to file for long storage"+getApplicationContext());
        }
        catch (IOException e){
            Log.e("EXCEPTION", e.toString());
        }

        Log.d(tag,"File updated");
        this.unregisterReceiver(receiver);
    }

}
