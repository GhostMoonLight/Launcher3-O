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

import com.android.launcher3.LauncherApplication;

import java.io.File;

/**
 * Created by cgx on 2017/11/21.
 *
 */

public class DUtil {

    /**
     * 是否已连接网络（未连接、wifi、2G、3G等）
     * @return
     */
    public static boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) LauncherApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
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
        Context context = LauncherApplication.getInstance();
        try {
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
        Context sContext = LauncherApplication.getInstance().getApplicationContext();
        File file = new File(filePath);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        sContext.startActivity(intent);
    }
}
