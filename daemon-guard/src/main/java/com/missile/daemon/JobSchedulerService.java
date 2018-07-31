package com.missile.daemon;

import android.app.job.JobParameters;
import android.app.job.JobService;


public class JobSchedulerService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        if (!DaemonEnv.sInitialized) {
            return false;
        }
        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
