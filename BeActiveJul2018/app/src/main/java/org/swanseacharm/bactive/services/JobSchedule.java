package org.swanseacharm.bactive.services;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;

public class JobSchedule {
    // schedule the start of the service at specific time
    public static void scheduleJob(Context context) {
        //Calenders to calculate how long in milliseconds to schedule the job for
        Calendar calendar = Calendar.getInstance();
        Calendar futureCalender = Calendar.getInstance();
        futureCalender.setTimeInMillis(System.currentTimeMillis());
        futureCalender.add(Calendar.DATE,+1);//Tomorrow
        futureCalender.set(Calendar.HOUR_OF_DAY,23);//11PM
        futureCalender.set(Calendar.MINUTE,58);//11:58PM
        futureCalender.set(Calendar.SECOND,0);//11:58:00PM

        calendar.setTimeInMillis(System.currentTimeMillis());

        //build job
        ComponentName serviceComponent = new ComponentName(context, JobScheduleService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(futureCalender.getTimeInMillis()-calendar.getTimeInMillis()); // wait at least until 11:58:00.000
        builder.setOverrideDeadline(futureCalender.getTimeInMillis()-calendar.getTimeInMillis()+900); // maximum delay 11:58:00.900
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.cancelAll();
        jobScheduler.schedule(builder.build());//schedule job
        Log.i("JobSchedule", "Save data job has been scheduled");

    }

}
