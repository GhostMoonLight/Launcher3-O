package com.android.launcher3;

import android.app.Application;
import android.content.Context;

import com.android.launcher3.logging.LogUtils;
import com.android.launcher3.util.Utils;

/**
 * Created by cuangengxuan on 2017/10/24.
 *
 */

public class LauncherApplication extends Application {

    private static LauncherApplication INSTANCE;



    public LauncherApplication(){
        LogUtils.eTag("LauncherApplication");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        String processName = Utils.getCurProcessName(this);
        // 只在主进程中初始化
        if (getPackageName().equals(processName)){

        }

    }

    public static LauncherApplication getINSTANCE(){
        return INSTANCE;
    }
}
