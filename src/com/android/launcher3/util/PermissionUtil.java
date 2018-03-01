package com.android.launcher3.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cgx on 2018/1/12.
 *
 */

public class PermissionUtil {
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    public static final int REQUEST_PERMISSION = 1000;
    public static final int REQUEST_PERMISSION_CAMERA = 1001;
    public static final int REQUEST_PERMISSION_READ_PHONE_STATE = 1002;
    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 1003;
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1004;

    // shouldShowRequestPermissionRationale()返回false 表示拒绝此权限，并不在询问。小米手机一直返回false


    // 申请某个权限
    public static void checkPermission(Activity activity, String perimission, int requestCode){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(perimission) !=
                    PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{perimission}, requestCode);
            }
        }
    }

    // 申请所有需要的权限
    public static void checkPermissionAll(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            long lastTime = PreferenceHelper.readLong(activity, PreferenceHelper.CONFIG, PreferenceHelper.CONFIG_LAST_PERMISSION_TIME);
            long interval = System.currentTimeMillis() - lastTime;
            if (interval < 0 || interval > 500 * 60 * 60 * 1000) {
                List<String> pers = new ArrayList<>();
                if (activity.checkSelfPermission(PERMISSION_WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    pers.add(PERMISSION_WRITE_EXTERNAL_STORAGE);
                }

                if (activity.checkSelfPermission(PERMISSION_READ_PHONE_STATE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    pers.add(PERMISSION_READ_PHONE_STATE);
                }

                if (activity.checkSelfPermission(PERMISSION_ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    pers.add(PERMISSION_ACCESS_FINE_LOCATION);
                }

                PreferenceHelper.write(activity, PreferenceHelper.CONFIG, PreferenceHelper.CONFIG_LAST_PERMISSION_TIME, System.currentTimeMillis());

                if (pers.size() > 0) {
                    int permissionCount = PreferenceHelper.readInt(activity, PreferenceHelper.CONFIG, PreferenceHelper.CONFIG_PERMISSION_COUNT, 0);
                    if (permissionCount < 1) {
                        permissionCount++;
                        activity.requestPermissions(pers.toArray(new String[pers.size()]), REQUEST_PERMISSION);
                        PreferenceHelper.write(activity, PreferenceHelper.CONFIG, PreferenceHelper.CONFIG_PERMISSION_COUNT, permissionCount);
                    } else {
                        // 提示用户去应用详情里面赋予权限
                        Toast.makeText(activity, "没有权限，请到应用详情里面赋予权限！", Toast.LENGTH_LONG).show();
                        Utils.startAppDetail(activity, activity.getPackageName());
                    }
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static void requestPermissions(Activity activity, @NonNull String[] permissions, int requestCode){
        int permissionCount = PreferenceHelper.readInt(activity, PreferenceHelper.CONFIG, PreferenceHelper.CONFIG_PERMISSION_COUNT, 0);
        if (permissionCount < 1) {
            permissionCount++;
            PreferenceHelper.write(activity, PreferenceHelper.CONFIG, PreferenceHelper.CONFIG_PERMISSION_COUNT, permissionCount);
            activity.requestPermissions(permissions, REQUEST_PERMISSION);
        } else {
            // 提示用户去应用详情里面赋予权限
            Toast.makeText(activity, "没有权限，请到应用详情里面赋予权限！", Toast.LENGTH_LONG).show();
            Utils.startAppDetail(activity, activity.getPackageName());
        }
    }

    // 判断是否拥有某个权限
    public static boolean isHasPermission(Context context, String perm) {
        PackageManager pm = context.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(perm, context.getPackageName()));

    }
}
