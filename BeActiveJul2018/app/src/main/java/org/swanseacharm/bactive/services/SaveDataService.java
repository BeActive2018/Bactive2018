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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveDataService extends Service {

    private int stepsSince12;

    private String tag="SaveDataSrvice";
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
        IntentFilter intentFilter = new IntentFilter("SaveDataService");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stepsSince12 = intent.getIntExtra("DATA_STEPS_TODAY",0);
            }
        };
        this.registerReceiver(receiver,intentFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy()
    {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void updateFile()
    {
        try{
            SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat formatterTime = new SimpleDateFormat("HHMMSS");
            Date date = new Date();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getBaseContext().openFileOutput(fileName,Context.MODE_APPEND));
            outputStreamWriter.write(stepsSince12+","+formatterDate.format(date)+"\n");
            Log.i(tag,"sent data to file for long storage");

        }
        catch (IOException e){
            Log.e("EXCEPTION", e.toString());
        }

        Log.i(tag,"File updated");
        stopSelf();
    }
}
