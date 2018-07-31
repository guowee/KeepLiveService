package com.missile.daemon;


import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

public final class DaemonEnv {

    public static final int DEFAULT_WAKE_UP_INTERVAL = 6 * 60 * 1000;
    private static final int MINIMAL_WAKE_UP_INTERVAL = 3 * 60 * 1000;
    static Context sApp;
    static Class<? extends AbsWorkService> sServiceClass;
    static boolean sInitialized;
    private static int sWakeUpInterval = DEFAULT_WAKE_UP_INTERVAL;

    static final Map<Class<? extends Service>, ServiceConnection> BIND_STATE_MAP = new HashMap<>();

    private DaemonEnv() {
    }

    public static void startServiceMayBind(final Class<? extends Service> serviceClass) {
        if (!sInitialized) {
            return;
        }
        final Intent intent = new Intent(sApp, serviceClass);
        startServiceSafely(intent);
        ServiceConnection bound = BIND_STATE_MAP.get(serviceClass);
        if (bound == null) {
            sApp.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    BIND_STATE_MAP.put(serviceClass, this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    BIND_STATE_MAP.remove(serviceClass);
                    startServiceSafely(intent);
                    if (!sInitialized) {
                        return;
                    }
                    sApp.bindService(intent, this, Context.BIND_AUTO_CREATE);
                }

                @Override
                public void onBindingDied(ComponentName name) {
                    onServiceDisconnected(name);
                }
            }, Context.BIND_AUTO_CREATE);
        }
    }


    public static void initialize(Context app, Class<? extends AbsWorkService> serviceClass, Integer wakeUpInterval) {
        sApp = app;
        sServiceClass = serviceClass;
        if (wakeUpInterval != null) {
            sWakeUpInterval = wakeUpInterval;
        }
        sInitialized = true;
    }


    static void startServiceSafely(Intent intent) {
        if (!sInitialized) {
            return;
        }
        sApp.startService(intent);
    }


    static int getWakeUpInterval() {
        return Math.max(sWakeUpInterval, MINIMAL_WAKE_UP_INTERVAL);
    }

}
