package com.missile.daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 监听 8 种系统广播 :
 * CONNECTIVITY_CHANGE, USER_PRESENT, ACTION_POWER_CONNECTED, ACTION_POWER_DISCONNECTED,
 * BOOT_COMPLETED, MEDIA_MOUNTED, PACKAGE_ADDED, PACKAGE_REMOVED.
 * 在网络连接改变, 用户屏幕解锁, 电源连接 / 断开, 系统启动完成, 挂载 SD 卡, 安装 / 卸载软件包时拉起 Service.
 * Service 内部做了判断，若 Service 已在运行，不会重复启动.
 * 运行在:watch子进程中.
 */

public class WakeUpReceiver extends BroadcastReceiver {

    protected static final String ACTION_CANCEL_JOB_ALARM_SUB = "com.missile.daemon.CANCEL_JOB_ALARM_SUB";

    @Override
    public void onReceive(Context context, Intent intent) {
        //监听ACTION_CANCEL_JOB_ALARM_SUB广播
        if (intent != null && ACTION_CANCEL_JOB_ALARM_SUB.equals(intent.getAction())) {
            WatchGuardService.cancelJobAlarmSub();
            return;
        }
        //监听USER_PRESENT，ACTION_POWER_CONNECTED，ACTION_POWER_DISCONNECTED广播
        if (!DaemonEnv.sInitialized) {
            return;
        }

        DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
    }

    public static class WakeUpAutoStartReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //监听BOOT_COMPLETED，CONNECTIVITY_CHANGE，PACKAGE_ADDED，PACKAGE_REMOVED广播
            if (!DaemonEnv.sInitialized) {
                return;
            }
            DaemonEnv.startServiceMayBind(DaemonEnv.sServiceClass);
        }
    }
}
