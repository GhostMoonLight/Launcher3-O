package com.android.launcher3.ImageCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.launcher3.R;
import com.android.launcher3.logging.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * Created by cgx on 2018/1/11.
 * 只能加载本地图片
 */
public class ImageCache {

    private static ImageCache INSTANCE;

    private LruCache<String, Bitmap> mMemoryCache;
    private HandlerThread mThread;
    private Handler mThreadHandler;
    private Handler mMainHandler;

    public ImageCache(Context context){
        //获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int mCacheSize = maxMemory / 8;
        //给LruCache分配1/8 4M
        mMemoryCache = new LruCache<String, Bitmap>(mCacheSize){

            //必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }

        };
        mThread = new HandlerThread("ImageCache");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mMainHandler = new Handler();

    }

    public static ImageCache getInstance(Context context){

        if (INSTANCE == null) {
            synchronized (ImageCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ImageCache(context);
                }
            }
        }

        return INSTANCE;
    }

    public void display(ImageView iv, String path){
        LogUtils.eTag("ImageCache display");
        mThreadHandler.post(new LoadRunnable(iv, path));
    }

    // 加载图片的Runnable
    class LoadRunnable implements Runnable{

        private WeakReference<ImageView> reference;
        private String path;

        LoadRunnable(ImageView imageView, String path){
            reference = new WeakReference<>(imageView);
            this.path = path;
            imageView.setTag(R.id.imageTag, path);
        }

        @Override
        public void run() {
            LogUtils.eTag("ImageCache run");
            Bitmap bitmap = mMemoryCache.get(path);
            if (bitmap == null) {
                // 获取图片
                File file = new File(path);
                if (file.exists()) {
                    try {
                        InputStream is = new FileInputStream(file);
                        BitmapFactory.Options decodingOptions = new BitmapFactory.Options();
                        decodingOptions.inSampleSize = 1;
                        bitmap = BitmapFactory.decodeStream(is, null, decodingOptions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null) {
                        // 保存到缓存中
                        mMemoryCache.put(path, bitmap);
                    }
                }
            }

            if (bitmap == null) {
                return;
            }

            // 显示图片
            final ImageView iv = reference.get();
            final Bitmap b = bitmap;
            if (iv != null) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String tagPath = (String) iv.getTag(R.id.imageTag);
                        if (tagPath != null && tagPath.equals(path)) {
                            iv.setImageBitmap(b);
                        }
                    }
                });
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight&& (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
