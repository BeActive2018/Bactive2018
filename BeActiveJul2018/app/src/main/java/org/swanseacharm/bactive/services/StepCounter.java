package org.swanseacharm.bactive.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StepCounter extends Service {
    public StepCounter() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
