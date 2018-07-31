package com.uowee.keeplive.demo;

import android.app.Application;

import com.missile.daemon.DaemonEnv;
import com.uowee.keeplive.service.SlaveService;


public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        DaemonEnv.initialize(this, SlaveService.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        SlaveService.sShouldStopService = false;
        DaemonEnv.startServiceMayBind(SlaveService.class);
    }
}
