package com.android.launcher3.download;

import android.view.View;

/**
 * Created by cgx on 2016/12/7.
 * 控制下载和View的刷新, 下载载体是以单个View为最小单元的
 */
public class DownloadController implements DownloadManager.DownloadObserver{

    private OnDownloadRefreshUI mTargView;
    private DownloadManager mDownloadManager;
    private int mState;
    private DownloadInfo info;

    public DownloadController(OnDownloadRefreshUI view){
        mTargView = view;
        mDownloadManager = DownloadManager.getInstance();
    }

    @Override
    public void onDownloadStateChanged(final DownloadTaskInfo info) {
        mState = info.downloadState;
        ((View)mTargView).post(new Runnable() {
            @Override
            public void run() {
                mTargView.onRefreshUI(info);
            }
        });
    }

    @Override
    public void onDownloadProgressed(final DownloadTaskInfo info) {
        mState = info.downloadState;
        ((View)mTargView).post(new Runnable() {
            @Override
            public void run() {
                mTargView.onRefreshUI(info);
            }
        });
    }

    public void registerObserver(){
        mDownloadManager.registerObserver(this);
    }

    public void unRegisterObserver(){
        mDownloadManager.unRegisterObserver(this);
    }

    //设置下载信息，如果该下载信息在下载任务中就刷新View
    public void setDwonloadInfo(DownloadInfo info){
        this.info = info;
        DownloadTaskInfo taskInfo = mDownloadManager.getDownloadMap().get(info.id);
        if (taskInfo != null){
            onDownloadStateChanged(taskInfo);
        }
    }

    //下载按钮的点击执行
    public void executeClick(DownloadInfo info){
        if (mState == DownloadManager.STATE_NONE || mState == DownloadManager.STATE_PAUSED || mState == DownloadManager.STATE_ERROR) {
            mDownloadManager.download(info);
        }else if (mState == DownloadManager.STATE_WAITING || mState == DownloadManager.STATE_DOWNLOADING) {
            mDownloadManager.pause(info);
        } else if (mState == DownloadManager.STATE_DOWNLOADED) {
//            PackageUtil.installApkNormal(DownloadTaskInfo.getPath(info.name));
        }
    }

    public void cancelDownload(DownloadInfo info){
        mDownloadManager.cancel(info);
    }

}
