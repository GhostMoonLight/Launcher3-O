package com.android.launcher3.download;

import android.text.TextUtils;

import com.android.launcher3.LauncherApplication;
import com.android.launcher3.util.Utils;

import java.util.ArrayList;

/**
 * Created by cgx on 2016/12/6.
 *
 */

public class DownloadTaskInfo {
    public int id;       //唯一标识
    public String name;  //名称
    public String url;    //地址
    public long size;    //大小

    private long currentSize = 0;//当前的size
    public int downloadState = 0;//下载的状态
    private String speed;
    public ArrayList<DownloadManager.DownloadTask> taskLists = new ArrayList<>();
    public int initState; //初始化状态 1:正在初始化，2：初始化完成，3:执行下载
    private int completeThreadCount;
    public long lastUpdateTime;  //上次刷新进度的时间
    public long oldDownloaded;   //获取速度时上次下载的长度

    public static DownloadTaskInfo clone(DownloadInfo downloadInfo){
        DownloadTaskInfo downloadTaskInfo = new DownloadTaskInfo();

        downloadTaskInfo.id = downloadInfo.id;
        downloadTaskInfo.name = downloadInfo.name;
        downloadTaskInfo.url = downloadInfo.url;
        downloadTaskInfo.size = downloadInfo.size;

        return downloadTaskInfo;
    }

    public float getCurrentProgress(){
        if (size == 0) return 0;
        float progress = currentSize*1.0f/size;
        if (progress < 0) progress = 0;
        return progress;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public int getId(){
        return id;
    }

    public void setDownloadState(int state){
        downloadState = state;
        if(state != DownloadManager.STATE_DOWNLOADING){
            speed = 0+"";
        }
    }

    public void setSpeed(long speed){
        if(speed < 0) speed = 0;
        this.speed = Utils.getDataSize(speed);
    }

    public String getSpeed(){
        if (TextUtils.isEmpty(speed)){
            return "0b/s";
        }
        return speed+"/s";
    }

    public void setCurrentSize(long currentSize){
        this.currentSize = currentSize;
    }

    public synchronized void addCurrentSize(long addSize){
        currentSize += addSize;
    }

    public long getCurrentSize(){
        return currentSize;
    }

    public String getUrl(){
        return url;
    }

    public String getPath(){
        return getPath(name);
    }

    public static String getPath(String name){
        return LauncherApplication.getInstance().getDoanloadDir()+"/"+name+".apk";
    }

    public synchronized void setCompleteThreadCount(){
        completeThreadCount++;
    }

    public int getCompleteThreadCount(){
        return completeThreadCount;
    }

    public DownloadTaskInfo cloneSelf() {
        DownloadTaskInfo info = new DownloadTaskInfo();

        info.id = id;
        info.initState = initState;
        info.downloadState = downloadState;
        info.name = name;
        info.url = url;
        info.size = size;
        info.completeThreadCount = completeThreadCount;

        for (DownloadManager.DownloadTask task : taskLists){
            info.taskLists.add(task.cloneSelf(info));
        }
        if (info.initState == 3){
            info.initState = 2;
        }
        info.oldDownloaded = info.currentSize;
        return info;
    }
}
