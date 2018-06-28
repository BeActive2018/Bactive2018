package org.swanseacharm.bactive.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverCall extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Service stopped","service stopped, restarting service");
        context.startService(new Intent(context,org.swanseacharm.bactive.services.StepCount.class));
    }
}
