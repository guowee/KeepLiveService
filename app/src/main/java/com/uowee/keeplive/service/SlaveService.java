package com.uowee.keeplive.service;

import android.content.Intent;
import android.os.IBinder;

import com.missile.daemon.AbsWorkService;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


public class SlaveService extends AbsWorkService {
    public static boolean sShouldStopService;

    public static Disposable sDisposable;

    @Override
    public boolean shouldStopService(Intent intent, int flag, int startId) {
        return sShouldStopService;
    }

    @Override
    public boolean isWorkRunning(Intent intent, int flag, int startId) {
        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public void startWork(Intent intent, int flag, int startId) {
        System.out.println("检查磁盘中是否有上次销毁时保存的数据");
        sDisposable = Observable
                .interval(3, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(() -> {
                    System.out.println("保存数据到磁盘。");
                    cancelJobAlarmSub();
                })
                .subscribe(count -> {
                    System.out.println("每 3 秒采集一次数据... count = " + count);
                    if (count > 0 && count % 18 == 0)
                        System.out.println("保存数据到磁盘。 saveCount = " + (count / 18 - 1));
                });
    }

    @Override
    public void stopWork(Intent intent, int flag, int startId) {
        stopService();
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        System.out.println("保存数据到磁盘.");
    }

    @Override
    public IBinder onBind(Intent intent, Void alwaysNull) {
        return null;
    }


    public static void stopService() {
        sShouldStopService = true;
        if (sDisposable != null) {
            sDisposable.dispose();
        }
        cancelJobAlarmSub();
    }
}
