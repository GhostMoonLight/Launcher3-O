package com.android.launcher3;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.launcher3.download.DownloadInfo;
import com.android.launcher3.download.DownloadManager;
import com.android.launcher3.download.DownloadTaskInfo;

import java.util.Map;

/**
 * Created by shajia on 17-11-16.
 */

public class TestActivity extends Activity implements DownloadManager.DownloadObserver{

    Button btn;
    TextView tv;
    int mState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);
        DownloadManager.getInstance().registerObserver(this);
        btn = findViewById(R.id.btn);
        tv = findViewById(R.id.pro_tv);

        String url = "http://wap.apk.anzhi.com/data3/apk/201711/02/3be3428e73a19b6a505e22f4ce2b96d0_39306300.apk";
        String url2 = "http://wap.apk.anzhi.com/data3/apk/201711/15/896b4e4c271d5e40fefc85855af2052e_85982100.apk";
        final DownloadInfo info = new DownloadInfo();
        info.name = "dd.apk";
        info.id = 1000;
        info.url = url;
        info.name = "dd1.apk";
        info.id = 1001;
        info.url = url2;

        Map<Integer, DownloadTaskInfo> downMap = DownloadManager.getInstance().getDownloadMap();
        if (!downMap.isEmpty()){
            DownloadTaskInfo tinfo = downMap.get(info.id);
            if (tinfo != null){
                refresh(tinfo);
            }
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == DownloadManager.STATE_NONE || mState == DownloadManager.STATE_PAUSED || mState == DownloadManager.STATE_ERROR) {
                    DownloadManager.getInstance().download(info);
                }else if (mState == DownloadManager.STATE_WAITING || mState == DownloadManager.STATE_DOWNLOADING) {
                    DownloadManager.getInstance().pause(info);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void onDownloadStateChanged(DownloadTaskInfo info) {
        refresh(info);
    }

    @Override
    public void onDownloadProgressed(DownloadTaskInfo info) {
        refresh(info);
    }

    private void refresh(final DownloadTaskInfo info){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (info.downloadState){
                    case DownloadManager.STATE_WAITING:
                        btn.setText("等待");
                        break;
                    case DownloadManager.STATE_PAUSED:
                        btn.setText("继续");

                        break;
                    case DownloadManager.STATE_DOWNLOADING:
                        btn.setText("暂停");
                        break;
                    case DownloadManager.STATE_DOWNLOADED:
                        btn.setText("完成");
                        break;
                    case DownloadManager.STATE_ERROR:
                        btn.setText("错误");
                        break;
                    default:
                        break;
                }
                tv.setText(info.getCurrentProgress()+"");
                mState = info.downloadState;
            }
        });
    }


}
