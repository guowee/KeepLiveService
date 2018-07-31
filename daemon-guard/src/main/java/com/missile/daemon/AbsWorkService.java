package com.missile.daemon;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;


public abstract class AbsWorkService extends Service {
    protected static final int HASH_CODE = 0x01;
    protected boolean mFirstStarted = true;


    public abstract boolean shouldStopService(Intent intent, int flag, int startId);

    /**
     * 判断任务是否还在运行
     *
     * @param intent
     * @param flag
     * @param startId
     * @return
     */
    public abstract boolean isWorkRunning(Intent intent, int flag, int startId);

    public abstract void startWork(Intent intent, int flag, int startId);

    public abstract void stopWork(Intent intent, int flag, int startId);

    public abstract void onServiceKilled(Intent rootIntent);

    public abstract IBinder onBind(Intent intent, Void alwaysNull);


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return onBind(intent, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId); //返回 START_STICKY
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    @Override
    public void onDestroy() {
        onEnd(null);
    }


    protected int onStart(Intent intent, int flag, int startId) {
        //启动守护进程，运行在:watch子进程中
        DaemonEnv.startServiceMayBind(WatchGuardService.class);

        boolean shouldStopService = shouldStopService(intent, flag, startId);
        if (shouldStopService) {
            stopService(intent, flag, startId);
        } else {
            startService(intent, flag, startId);
        }

        if (mFirstStarted) {
            mFirstStarted = false;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                //API Level 17 及以下的Android系统中，启动前台服务而不显示通知
                startForeground(HASH_CODE, new Notification());
                //API Level 18 及以上的Android系统中，启动前台服务而不显示通知
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    DaemonEnv.startServiceSafely(new Intent(getApplication(), InnerService.class));
                }
            }
            //守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用
            getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), WatchGuardService.class.getName()),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        return START_STICKY;
    }

    protected void onEnd(Intent rootIntent) {
        onServiceKilled(rootIntent);
        if (!DaemonEnv.sInitialized) {
            return;
        }
        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        DaemonEnv.startServiceMayBind(WatchGuardService.class);

    }


    void startService(Intent intent, int flag, int startId) {
        boolean shouldStopService = shouldStopService(intent, flag, startId);
        if (shouldStopService) {
            return;
        }
        boolean workRunning = isWorkRunning(intent, flag, startId);
        if (workRunning) {
            return;
        }

        startWork(intent, flag, startId);
    }

    void stopService(Intent intent, int flag, int startId) {
        stopWork(intent, flag, startId);
        cancelJobAlarmSub();
    }

    /**
     * 用于在不需要服务运行的时候取消 Job / Alarm / Subscription.(通过广播)
     */
    public static void cancelJobAlarmSub() {
        if (!DaemonEnv.sInitialized) {
            return;
        }
        DaemonEnv.sApp.sendBroadcast(new Intent(WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB));
    }

    /**
     * 从 Android2.3 开始调用 setForeground 将后台 Service 设置为前台 Service 时，必须在系统的通知栏发送一条通知，
     * 也就是前台 Service 与一条可见的通知时绑定在一起的。
     * 解决措施：通过实现一个内部 Service，在 LiveService 和其内部 Service 中同时发送具有相同 ID 的 Notification，
     * 然后将内部 Service 结束掉。随着内部 Service 的结束，Notification 将会消失，但系统优先级依然保持为2。
     */
    public static class InnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(AbsWorkService.HASH_CODE, new Notification());
            stopSelf();
            return START_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

}
