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

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by shajia on 17-11-16.
 */

public class TestActivity extends Activity implements DownloadManager.DownloadObserver, View.OnClickListener {

    Button btn, btn2, btn3, btn4, btn5;
    TextView tv, tv2, tv3, tv4, tv5;
    int mState;

    ArrayList<DownloadInfo> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_test);
        DownloadManager.getInstance().registerObserver(this);
        btn = findViewById(R.id.btn);
        tv = findViewById(R.id.pro_tv);

        btn2 = findViewById(R.id.btn2);
        tv2 = findViewById(R.id.pro_tv2);

        btn3 = findViewById(R.id.btn3);
        tv3 = findViewById(R.id.pro_tv3);

        btn4 = findViewById(R.id.btn4);
        tv4 = findViewById(R.id.pro_tv4);

        btn5 = findViewById(R.id.btn5);
        tv5 = findViewById(R.id.pro_tv5);

        String url = "http://wap.apk.anzhi.com/data3/apk/201711/02/3be3428e73a19b6a505e22f4ce2b96d0_39306300.apk";
        String url2 = "http://wap.apk.anzhi.com/data3/apk/201711/15/896b4e4c271d5e40fefc85855af2052e_85982100.apk";
        String url3 = "http://wap.apk.anzhi.com/data3/apk/201710/17/81e485109892edfa439dad0173ae1f1d_06692700.apk";
        String url4 = "http://wap.apk.anzhi.com/data3/apk/201709/14/b0c1ceb056787d22e72cb9a6afd351d6_54392900.apk";
        String url5 = "http://wap.apk.anzhi.com/data1/apk/201711/16/76dd2622ef48bb471eb320fafe31b7b0_54574600.apk";
        DownloadInfo info = new DownloadInfo();
        info.name = "dd.apk";
        info.id = 1000;
        info.url = url;
        btn.setTag(info);
        list.add(info);

        info = new DownloadInfo();
        info.name = "dd1.apk";
        info.id = 1001;
        info.url = url2;
        btn2.setTag(info);
        list.add(info);

        info = new DownloadInfo();
        info.name = "dd2.apk";
        info.id = 1002;
        info.url = url3;
        btn3.setTag(info);
        list.add(info);

        info = new DownloadInfo();
        info.name = "dd3.apk";
        info.id = 1003;
        info.url = url4;
        btn4.setTag(info);
        list.add(info);

        info = new DownloadInfo();
        info.name = "dd4.apk";
        info.id = 1004;
        info.url = url5;
        btn5.setTag(info);
        list.add(info);

        Map<Integer, DownloadTaskInfo> downMap = DownloadManager.getInstance().getDownloadMap();
        if (!downMap.isEmpty()){
            for (DownloadInfo i: list) {
                DownloadTaskInfo tinfo = downMap.get(i.id);
                if (tinfo != null) {
                    refresh(tinfo);
                }
            }
        }

        btn.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);

        Button button = findViewById(R.id.btn_del);
        button.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadManager.getInstance().unRegisterObserver(this);
    }

    @Override
    public void onDownloadProgressed(DownloadTaskInfo info) {
        refresh(info);
    }

    private void refresh(final DownloadTaskInfo info){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DownloadInfo dInfo = null;
                for(DownloadInfo di: list){
                    if (di.id == info.id)
                        dInfo = di;
                }

                Button btn = null;
                TextView tv = null;
                if (info.id == 1000){
                    btn = TestActivity.this.btn;
                    tv = TestActivity.this.tv;
                } else if (info.id == 1001){
                    btn = TestActivity.this.btn2;
                    tv = TestActivity.this.tv2;
                } else if (info.id == 1002){
                    btn = TestActivity.this.btn3;
                    tv = TestActivity.this.tv3;
                } else if (info.id == 1003){
                    btn = TestActivity.this.btn4;
                    tv = TestActivity.this.tv4;
                } else if (info.id == 1004){
                    btn = TestActivity.this.btn5;
                    tv = TestActivity.this.tv5;
                }

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
                    case DownloadManager.STATE_NONE:
                        btn.setText("下载");
                        break;
                    default:
                        break;
                }
                tv.setText(info.getCurrentProgress()+"");
                dInfo.mState = info.downloadState;
            }
        });
    }


    @Override
    public void onClick(View v) {
        DownloadInfo info = (DownloadInfo) v.getTag();
        if (info != null) {
            if (info.mState == DownloadManager.STATE_NONE || info.mState == DownloadManager.STATE_PAUSED || info.mState == DownloadManager.STATE_ERROR) {
                DownloadManager.getInstance().download(info);
            } else if (info.mState == DownloadManager.STATE_WAITING || info.mState == DownloadManager.STATE_DOWNLOADING) {
                DownloadManager.getInstance().pause(info);
            }
        } else {
            for (DownloadInfo di : list) {
                DownloadManager.getInstance().cancel(di);
            }
        }
    }
}
