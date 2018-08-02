package org.swanseacharm.bactive.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.swanseacharm.bactive.services.JobSchedule;

import java.util.concurrent.locks.ReentrantLock;

public class BootReceive extends BroadcastReceiver {//receives in event of boot

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.getAction();
        Intent newIntent = new Intent("org.swanseacharm.bactive.services").putExtra("DATA_FROM_BOOT",true);//intent to send to StepCounter.class. Tells StepCounter.class that a boot just happened.
        ReentrantLock lock = new ReentrantLock();//locks for waiting
        try {
            lock.lock();
            wait(100);//wait so StepCounter.class has time to restart
            lock.unlock();
        }
        catch (InterruptedException e)
        {
            Log.e("BootReceive",e.toString());
        }

        Log.d("BootReceive","Sending broadcast");
        context.sendBroadcast(newIntent);//send broadcast
        JobSchedule.scheduleJob(context);//schedule tasks (SaveDataService.class)
    }
}
