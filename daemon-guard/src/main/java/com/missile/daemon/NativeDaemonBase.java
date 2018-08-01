package com.missile.daemon;


import android.content.Context;

public class NativeDaemonBase {

    protected Context mContext;

    public NativeDaemonBase(Context context) {
        this.mContext = context;
    }

    /**
     * native call back
     */
    protected void onDaemonDead() {
        IDaemonStrategy.Fetcher.fetchStrategy().onDaemonDead();
    }


}
