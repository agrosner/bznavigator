package edu.fordham.cis.wisdm.zoo.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceOps {

    public static boolean isRunning(Class<?> c, Context mCtx) {
        ActivityManager manager = (ActivityManager) mCtx.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (c.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

