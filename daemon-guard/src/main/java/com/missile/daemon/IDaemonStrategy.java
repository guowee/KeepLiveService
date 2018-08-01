package com.missile.daemon;


import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.missile.daemon.strategy.DaemonStrategyUnder21;

public interface IDaemonStrategy {

    void onPersistentCreate(Context context, Class wakeUpClass);

    void onDaemonDead();

    class Fetcher {

        private static IDaemonStrategy mDaemonStrategy;

        /**
         * fetch the strategy for this device
         *
         * @return the daemon strategy for this device
         */
        static IDaemonStrategy fetchStrategy() {
            if (mDaemonStrategy != null) {
                return mDaemonStrategy;
            }
            int sdk = Build.VERSION.SDK_INT;
            Log.e("TAG", "----------SDK = " + sdk);
            switch (sdk) {
                case 23:
                    break;
                case 22:
                    break;
                case 21:
                    mDaemonStrategy = new DaemonStrategyUnder21();
                    break;
                default:
                    mDaemonStrategy = new DaemonStrategyUnder21();
                    break;
            }
            return mDaemonStrategy;
        }
    }
}
