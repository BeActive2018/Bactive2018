package org.swanseacharm.bactive.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverCall extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(ReceiverCall.class.getSimpleName(),"service stopped, restarting service");
        context.startService(new Intent(context,org.swanseacharm.bactive.services.StepCounter.class)
                .putExtra("DATA_STEPS_SINCE_TWELVE",intent.getIntExtra("DATA_STEPS_SINCE_TWELVE",0))
                .putExtra("DATA_OLD_STEPS",intent.getIntExtra("DATA_OLD_STEPS",0))
                .putExtra("DATA_LAST_SAVE",intent.getStringExtra("DATA_LAST_SAVE")));

    }
}
