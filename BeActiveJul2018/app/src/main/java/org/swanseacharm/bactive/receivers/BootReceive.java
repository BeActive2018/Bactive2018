package org.swanseacharm.bactive.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.swanseacharm.bactive.services.JobSchedule;

public class BootReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent("org.swanseacharm.bactive.services").putExtra("DATA_FROM_BOOT",true);
        try {
            wait(600);
        }
        catch (InterruptedException e)
        {
            Log.e("BootReceive",e.toString());
        }
        Log.i("BootReceive","Sending broadcast");
        context.sendBroadcast(newIntent);
        JobSchedule.scheduleJob(context);
    }
}
