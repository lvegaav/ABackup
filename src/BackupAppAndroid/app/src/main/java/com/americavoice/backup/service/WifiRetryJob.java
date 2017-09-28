package com.americavoice.backup.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.americavoice.backup.Const;
import com.americavoice.backup.utils.JobSchedulerUtils;
import com.americavoice.backup.utils.WifiUtils;

/**
 * Created by javier on 9/21/17.
 * Job to retry failed transactions on wifi connection.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class WifiRetryJob extends JobService {

    static final JobInfo JOB_INFO;
    private static final String TAG = "WifiRetryJob";

    static {
        JOB_INFO = new JobInfo.Builder(JobIds.WIFI_RETRY_JOB,
                new ComponentName(Const.AUTHORITY, WifiRetryJob.class.getName()))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .build();
    }


    // Schedule this job, replace any existing one.
    public static void scheduleJob(Context context) {
        JobSchedulerUtils.scheduleJob(context, JOB_INFO);
    }

    // Cancel this job, if currently scheduled.
    public static void cancelJob(Context context) {
        JobSchedulerUtils.cancelJob(context, JobIds.WIFI_RETRY_JOB);
    }


    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i(TAG, "Wifi job started");
        WifiUtils.wifiConnected(getApplicationContext());
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}
