package org.swanseacharm.bactive.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class JobScheduleService extends JobService {
    private static final String tag = "JobScheduleService";

    @Override
    public boolean onStartJob(JobParameters params) {//called when job starts
        Intent service = new Intent(getApplicationContext(), SaveDataService.class);
        getApplicationContext().startService(service);//start the SaveDataService service
        JobSchedule.scheduleJob(getApplicationContext()); // reschedule the job
        Log.i(tag,"Job started, rescheduled next job");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}
