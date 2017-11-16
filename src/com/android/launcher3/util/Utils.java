package com.android.launcher3.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

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

    /**
     * 返回byte的数据大小对应的文本
     */
    public static String getDataSize(long size) {
        DecimalFormat formater = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + "bytes";
        } else if (size < 1024 * 1024) {
            float kbsize = size / 1024f;
            return formater.format(kbsize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbsize = size / 1024f / 1024f;
            return formater.format(mbsize) + "MB";
        } else if (size/1024f < 1024 * 1024 * 1024) {
            float gbsize = size / 1024f / 1024f / 1024f;
            return formater.format(gbsize) + "GB";
        } else {
            return "size: error";
        }
    }

    /**
     * 判断sdcard是否可用
     *
     * @return
     */
    public static boolean isSDCardAvailable() {
        boolean sdcardAvailable = false;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdcardAvailable = true;
        }
        return sdcardAvailable;
    }
}
