package com.android.launcher3.download;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by cgx on 2017/11/21.
 *
 */

class DUtil {

    /**
     * 是否已连接网络（未连接、wifi、2G、3G等）
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null) {
            return netInfo.isConnected();
        } else {
            return false;
        }
    }

    static public class AppSnippet {
        public CharSequence label;
        public Drawable icon;
        public String packageName;
        public String versionName;
        int versionCode;
    }

    /**
     * 获取安装包的App信息
     * @param apkPath
     * @return
     */
    public static AppSnippet getAppSnippet(String apkPath) {
        AppSnippet sAppSnippet = null;
        try {
            Context context = DownloadManager.getContext();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            //			Resources sResources = getUninstalledApkResources(context, apkPath);

            // 读取apk文件的信息
            if (packageInfo != null) {
                sAppSnippet = new AppSnippet();
                //				sAppSnippet.icon = sResources.getDrawable(packageInfo.applicationInfo.icon);// 图标
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                appInfo.sourceDir = apkPath;
                appInfo.publicSourceDir = apkPath;
                sAppSnippet.icon = appInfo.loadIcon(pm);
                sAppSnippet.packageName = packageInfo.packageName;		// 包名
                sAppSnippet.versionName = packageInfo.versionName;		// 版本号
                sAppSnippet.versionCode = packageInfo.versionCode;		// 版本码
                //				sAppSnippet.permissions = packageInfo.requestedPermissions;

                try {
                    //					sAppSnippet.label = (String) sResources.getText(packageInfo.applicationInfo.labelRes);// 名字
                    sAppSnippet.label = appInfo.loadLabel(pm);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        sAppSnippet.label = pm.getApplicationLabel(packageInfo.applicationInfo);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sAppSnippet;
    }

    /**
     * 安装应用
     * @param filePath
     */
    public static void installApkNormal(String filePath) {
        Context context = DownloadManager.getContext();
        File file = new File(filePath);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
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

    /**
     * 下载目录
     */
    public static String getDoanloadDir() {
        String strCacheDir;
        File cacheDir;
        File cacheFile;

        Context context = DownloadManager.getContext();
        if (isSDCardAvailable()) {
            cacheDir = context.getExternalCacheDir();

            if (cacheDir == null) {
                cacheDir = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + context.getPackageName() + "/cache");
                if (!cacheDir.exists()){
                    if (!cacheDir.mkdirs()){
                        DLog.eTag("创建DoanloadDir 失败");
                    }
                }
            }
        } else {
            cacheDir = context.getCacheDir();

        }
        cacheFile = new File(cacheDir, "download");
        if (!cacheFile.exists()){
            if (!cacheFile.mkdirs()){
                DLog.eTag("创建Download 缓存Dir 失败");
            }
        }
        strCacheDir = cacheFile.getAbsolutePath();
        return strCacheDir;
    }
}
