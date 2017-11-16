package com.android.launcher3;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.android.launcher3.logging.LogUtils;
import com.android.launcher3.util.Utils;

import java.io.File;

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

    public static LauncherApplication getInstance(){
        return INSTANCE;
    }

    /**
     * 下载目录
     */
    public String getDoanloadDir() {
        String strCacheDir;
        File cacheDir;
        File cacheFile;

        if (Utils.isSDCardAvailable()) {
            cacheDir = getExternalCacheDir();

            if (cacheDir == null) {
                cacheDir = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + getPackageName() + "/cache");
                if (!cacheDir.exists()){
                    if (!cacheDir.mkdirs()){
                        LogUtils.eTag("创建DoanloadDir 失败");
                    }
                }
            }
        } else {
            cacheDir = getCacheDir();

        }
        cacheFile = new File(cacheDir, "download");
        if (!cacheFile.exists()){
            if (!cacheFile.mkdirs()){
                LogUtils.eTag("创建Download 缓存Dir 失败");
            }
        }
        strCacheDir = cacheFile.getAbsolutePath();
        return strCacheDir;
    }
}
