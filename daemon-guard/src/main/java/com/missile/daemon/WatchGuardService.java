package com.missile.daemon;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class WatchGuardService extends Service {


    protected static final int HASH_CODE = 0x02;
    protected static Disposable sDisposable;
    protected static PendingIntent sPendingIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return onStart(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        onEnd(rootIntent);
    }

    @Override
    public void onDestroy() {
        onEnd(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        onStart(intent, 0, 0);
        return null;
    }

    protected final int onStart(Intent intent, int flag, int startId) {
        if (!DaemonEnv.sInitialized) {
            return START_STICKY;
        }
        if (sDisposable != null && !sDisposable.isDisposed()) {
            return START_STICKY;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            startForeground(HASH_CODE, new Notification());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                DaemonEnv.startServiceSafely(new Intent(DaemonEnv.sApp, InnerService.class));
            }
        }

        //定时检查AbsWorkService是否在运行，如果不在运行就把它拉起来
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Android 5.0+ 使用 JobScheduler
            //Android5.0 以后系统对 Native 进程等加强了管理，Native 拉活方式失效。系统在 Android5.0 以上版本提供了 JobScheduler 接口，系统会定时调用该进程以使应用进行一些逻辑操作
            JobInfo.Builder builder = new JobInfo.Builder(HASH_CODE, new ComponentName(DaemonEnv.sApp, JobSchedulerService.class));
            builder.setPeriodic(DaemonEnv.getWakeUpInterval());
            //Android 7.0+ 增加了一项针对 JobScheduler 的新限制，最小间隔只能是下面设定的数字
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(JobInfo.getMinPeriodMillis(), JobInfo.getMinFlexMillis());
            }
            builder.setPersisted(true);
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        } else {
            //Android 4.4- 使用 AlarmManager
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent in = new Intent(DaemonEnv.sApp, DaemonEnv.sServiceClass);
            sPendingIntent = PendingIntent.getService(DaemonEnv.sApp, HASH_CODE, in, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + DaemonEnv.getWakeUpInterval(),
                    DaemonEnv.getWakeUpInterval(), sPendingIntent);
        }
        //使用定时 Observable，避免 Android 定制系统 JobScheduler / AlarmManager 唤醒间隔不稳定的情况
        sDisposable = Observable.interval(DaemonEnv.getWakeUpInterval(), TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });

        //使用Native的方式
        IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(DaemonEnv.sApp, DaemonEnv.sServiceClass);


        //守护 Service 组件的启用状态, 使其不被 MAT 等工具禁用
        getPackageManager().setComponentEnabledSetting(new ComponentName(getPackageName(), DaemonEnv.sServiceClass.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        return START_STICKY;
    }

    protected void onEnd(Intent rootIntent) {
        if (!DaemonEnv.sInitialized) {
            return;
        }

        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        DaemonEnv.startServiceMayBind(WatchGuardService.class);
    }

    /**
     * 用于在不需要服务运行的时候取消 Job / Alarm / Subscription.
     * <p>
     * 因 WatchGuardService 运行在 :watch 子进程, 请勿在主进程中直接调用此方法.
     * 而是向 WakeUpReceiver 发送一个 Action 为 WakeUpReceiver.ACTION_CANCEL_JOB_ALARM_SUB 的广播.
     */
    public static void cancelJobAlarmSub() {
        if (!DaemonEnv.sInitialized) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler scheduler = (JobScheduler) DaemonEnv.sApp.getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancel(HASH_CODE);
        } else {
            AlarmManager alarmManager = (AlarmManager) DaemonEnv.sApp.getSystemService(ALARM_SERVICE);
            if (sPendingIntent != null) {
                alarmManager.cancel(sPendingIntent);
            }
        }

        if (sDisposable != null) {
            sDisposable.dispose();
        }
    }


    public static class InnerService extends Service {
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(WatchGuardService.HASH_CODE, new Notification());
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
