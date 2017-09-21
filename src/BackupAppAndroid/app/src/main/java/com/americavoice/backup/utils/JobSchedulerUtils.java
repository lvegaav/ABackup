package com.americavoice.backup.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.americavoice.backup.service.JobIds;

import java.util.List;

/**
 * Created by javier on 9/21/17.
 * Utils to for job scheduler
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class JobSchedulerUtils {

    private final static String TAG = "JobSchedulerUtils";

    public static boolean isScheduled(Context context, int jobId) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = js.getAllPendingJobs();
        for (int i=0; i<jobs.size(); i++) {
            if (jobs.get(i).getId() == jobId) {
                return true;
            }
        }
        return false;
    }

    public static void scheduleJob(Context context, JobInfo jobInfo) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.schedule(jobInfo);
        Log.i(TAG, "JOB SCHEDULED WITH ID: " + jobInfo.getId());
    }


    public static void cancelJob(Context context, int jobId) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        if (isScheduled(context, jobId)) {
            js.cancel(jobId);
        }
        Log.i(TAG, "Job with id: " + jobId + " was cancelled");
    }
}
