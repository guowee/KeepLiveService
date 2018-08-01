package com.missile.daemon;


import android.content.Context;

public class NativeDaemonAPI21 extends NativeDaemonBase {

    static {
        System.loadLibrary("daemon_api21");
    }


    public NativeDaemonAPI21(Context context) {
        super(context);
    }


    public native void doDaemon(String indicatorSelfPath, String indicatorDaemonPath, String observerSelfPath, String observerDaemonPath);
}
