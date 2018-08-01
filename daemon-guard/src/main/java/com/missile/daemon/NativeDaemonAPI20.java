package com.missile.daemon;


import android.content.Context;

public class NativeDaemonAPI20 extends NativeDaemonBase {

    static {
        System.loadLibrary("daemon_api20");
    }


    public NativeDaemonAPI20(Context context) {
        super(context);
    }

    public native void doDaemon(String pkgName, String svcName, String daemonPath);
}
