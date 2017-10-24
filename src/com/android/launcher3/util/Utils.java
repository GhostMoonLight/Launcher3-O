package com.android.launcher3.util;

import android.app.ActivityManager;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Created by cuangengxuan on 2017/10/24.
 */

public class Utils {

    public static String getCurProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context. ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if ( appProcess.pid == android.os.Process.myPid()) {
                return appProcess.processName;
            }
        }

        try {
            Class<?> clazz = Class.forName("android.ddm.DdmHandleAppName");
            Method getAppNameMethod = clazz.getDeclaredMethod("getAppName");
            return (String)getAppNameMethod.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
