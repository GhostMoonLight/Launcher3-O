package com.android.launcher3.download;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by cgx on 2016/12/6.
 */

public class DownloadInfo implements Parcelable,Serializable {

    public int id;       //唯一标识
    public String name;  //名称
    public String url;    //地址
    public long size;    //大小
    public int mState;    // 下载状态

    public DownloadInfo() {
    }

    protected DownloadInfo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
        size = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeLong(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DownloadInfo> CREATOR = new Creator<DownloadInfo>() {
        @Override
        public DownloadInfo createFromParcel(Parcel in) {
            return new DownloadInfo(in);
        }

        @Override
        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
}
