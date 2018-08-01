package com.missile.daemon.strategy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.missile.daemon.Command;
import com.missile.daemon.IDaemonStrategy;
import com.missile.daemon.NativeDaemonAPI20;


public class DaemonStrategyUnder21 implements IDaemonStrategy {
    private static final String DIR_NAME = "bin";
    private static final String FILE_NAME = "daemon";

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;

    @Override
    public void onPersistentCreate(final Context context, final Class wakeUpClass) {
        Log.e("TAG", "-----SERVICE NAME = " + wakeUpClass.getSimpleName());
        initAlarm(context, wakeUpClass.getSimpleName());
        Thread thread = new Thread() {
            @Override
            public void run() {
                String binaryFilePath = Command.install(context, DIR_NAME, FILE_NAME);
                new NativeDaemonAPI20(context).doDaemon(
                        context.getPackageName(),
                        wakeUpClass.getSimpleName(),
                        binaryFilePath);
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public void onDaemonDead() {
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 100, mPendingIntent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void initAlarm(Context context, String serviceName) {
        if (mAlarmManager == null) {
            mAlarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
        }
        if (mPendingIntent == null) {
            Intent intent = new Intent();
            ComponentName component = new ComponentName(context.getPackageName(), serviceName);
            intent.setComponent(component);
            intent.setFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
            mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
        }
        mAlarmManager.cancel(mPendingIntent);
    }


}
