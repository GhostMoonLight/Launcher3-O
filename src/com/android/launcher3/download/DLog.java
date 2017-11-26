package com.android.launcher3.download;


import android.util.Log;

import com.android.launcher3.util.Const;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by cgx on 16/11/10.
 *
 */
class DLog {
    private static boolean mLogEnable = Const.DEBUG;
    private static String mClassname = DLog.class.getName();
    private static ArrayList<String> mMethods = new ArrayList<>();
    private static String TAG = "AAAAA";

    public static void setEnable(boolean logEnable) {
        mLogEnable = logEnable;
    }

    public static void d(String tag, String msg) {
        if (mLogEnable) {
            Log.d(tag, getMsgWithLineNumber(msg));
        }

    }

    public static void e(String tag, String msg) {
        if (mLogEnable) {
            Log.e(tag, getMsgWithLineNumber(msg));
        }

    }

    public static void i(String tag, String msg) {
        if (mLogEnable) {
            Log.i(tag, getMsgWithLineNumber(msg));
        }

    }

    public static void w(String tag, String msg) {
        if (mLogEnable) {
            Log.w(tag, getMsgWithLineNumber(msg));
        }

    }

    public static void v(String tag, String msg) {
        if (mLogEnable) {
            Log.v(tag, getMsgWithLineNumber(msg));
        }

    }

    public static void d(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.d(content[0], content[1]);
        }

    }

    static void eTag(String msg){
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.e(TAG, content[0]+"."+content[1]);
        }
    }

    static void deTag(String msg){
        if (mLogEnable) {
            Log.e(TAG, msg);
        }
    }

    static void dTag(String msg){
        if (mLogEnable) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.e(content[0], content[1]);
        }

    }

    public static void i(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.i(content[0], content[1]);
        }

    }

    public static void i() {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber("");
            Log.i(content[0], content[1]);
        }

    }

    public static void w(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.w(content[0], content[1]);
        }

    }

    public static void v(String msg) {
        if (mLogEnable) {
            String[] content = getMsgAndTagWithLineNumber(msg);
            Log.v(content[0], content[1]);
        }

    }

    static String getMsgWithLineNumber(String msg) {
        try {
            StackTraceElement[] e = (new Throwable()).getStackTrace();
            int var2 = e.length;

            for (StackTraceElement st : e) {
                if (!mClassname.equals(st.getClassName()) && !mMethods.contains(st.getMethodName())) {
                    int b = st.getClassName().lastIndexOf(".") + 1;
                    String TAG = st.getClassName().substring(b);
                    return TAG + "->" + st.getMethodName() + "():" + st.getLineNumber() + "->" + msg;
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

        return msg;
    }

    static String[] getMsgAndTagWithLineNumber(String msg) {
        try {
            StackTraceElement[] e = (new Throwable()).getStackTrace();
            for (StackTraceElement st : e) {
                if (!mClassname.equals(st.getClassName()) && !mMethods.contains(st.getMethodName())) {
                    int b = st.getClassName().lastIndexOf(".") + 1;
                    String TAG = st.getClassName().substring(b);
                    String message = st.getMethodName() + "():" + st.getLineNumber() + "->" + msg;
                    return new String[]{TAG, message};
                }
            }
        } catch (Exception var9) {
        }

        return new String[]{"universal tag", msg};
    }

    static {
        Method[] ms = DLog.class.getDeclaredMethods();
        for (Method m : ms) {
            mMethods.add(m.getName());
        }
    }
}
