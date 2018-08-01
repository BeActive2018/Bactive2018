package org.swanseacharm.bactive.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import org.swanseacharm.bactive.Util;
import org.swanseacharm.bactive.ui.Yesterday;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SaveDataService extends Service {

    private int stepsSince12;

    private String tag="SaveDataService";
    private String fileName="stepHistory.stp";

    private BroadcastReceiver receiver;
    public SaveDataService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(tag,"onCreate called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag,"onStartCommand called");
        IntentFilter intentFilter = new IntentFilter("org.swanseacharm.bactive.FRESHDATA");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stepsSince12 = intent.getIntExtra("DATA_STEPS_TODAY",0);
                updateFile();
                Log.i(tag,"Fresh data received");
                stopSelf();

            }
        };
        this.registerReceiver(receiver,intentFilter);
        Intent sendIntent = new Intent("org.swanseacharm.bactive.SAVEDATA")
                .putExtra("REQUEST_FRESH_DATA",true)
                .putExtra("COMMAND_RESTART_SERVICE",true);
        sendBroadcast(sendIntent);
        Log.i(tag,"Fresh Data request + RESTART command");
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy()
    {

        super.onDestroy();
    }

    public void updateFile()
    {
        try{
            SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
            //Date date = new Date();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DATE,+1);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getBaseContext().openFileOutput(fileName,Context.MODE_APPEND));
            outputStreamWriter.write(stepsSince12+","+formatterDate.format(calendar.getTimeInMillis())+"/n");
            outputStreamWriter.close();
            Log.d(tag,stepsSince12+","+formatterDate.format(calendar.getTimeInMillis())+"/n");
            Log.i(tag,"sent data to file for long storage"+getApplicationContext());
            //getFileFull(getBaseContext());
        }
        catch (IOException e){
            Log.e("EXCEPTION", e.toString());
        }

        Log.i(tag,"File updated");
        this.unregisterReceiver(receiver);
    }

}
