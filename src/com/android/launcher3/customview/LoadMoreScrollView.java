package com.android.launcher3.customview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by cgx on 2018/3/21.
 *ScrollView的LinearLayout最后添加
 * <TextView
     android:id="@+id/load"
     android:layout_width="match_parent"
     android:layout_height="0dp"
     android:padding="5dp"
     android:textColor="@color/live_class_title_select"
     android:textSize="16sp"
     android:gravity="center"
     android:text="松手加载"/>

    添加之后actionMove中106行获取load = (TextView) findViewById(R.id.load);的注释去掉
 */

public class LoadMoreScrollView extends NestedScrollView {

    private TextView load;
    private int scrollY = 0, loadHeight;
    private int mScaledTouchSlop;
    private int startDownY, moveDownY, lastDwonY;
    private float startY;
    private LinearLayout.LayoutParams loadParams = null;
    private ValueAnimator vaScroll, vaLoad;  //滑动的属性动画
    private int maxLoadHeight = 50;
    private boolean isLoadMore;
    private boolean isNeedLoadMore;

    public LoadMoreScrollView(Context context) {
        super(context);
        init();
    }

    public LoadMoreScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (isNeedLoadMore){
            moveDownY = (int) ev.getRawY();

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastDwonY = startDownY = moveDownY;
                    startY = ev.getY();
                    loadHeight = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    actionMove();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    actionUp();
                    break;
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    public void setIsNeedLoadMore(boolean isNeedLoadMore){
        this.isNeedLoadMore = isNeedLoadMore;
    }

    private void actionMove(){

        //上拉刷新
        if (load == null) {
//            load = (TextView) findViewById(R.id.load);
        }

        if ((scrollY+getHeight() == getChildAt(0).getMeasuredHeight())
                && !(load.getHeight() >= dip2px(maxLoadHeight))){
            //滑动到底部
            loadParams = (LinearLayout.LayoutParams) load.getLayoutParams();
            loadHeight = loadHeight + (lastDwonY - moveDownY);
            if (loadHeight < 0){
                loadHeight = 0;
            }
            if (loadHeight > dip2px( maxLoadHeight)){
                loadHeight = dip2px(maxLoadHeight);
            }
            loadParams.height = loadHeight;
            load.setLayoutParams(loadParams);
            invalidate();
            load.setText("松手加载...");
        }

        lastDwonY = moveDownY;
    }

    private void actionUp() {
        if (scrollBottomListener != null) {
            if (load != null && !isLoadMore && load.getHeight() >= dip2px(maxLoadHeight)) {
                //加载数据
                isLoadMore = true;
                load.setText("正在加载...");
                scrollBottomListener.loadData();
                if (loadParams == null)
                    loadParams = (LinearLayout.LayoutParams) load.getLayoutParams();

                int maxHeight =  dip2px(maxLoadHeight);
                if (loadParams.height < maxHeight) {
                    vaLoad = ValueAnimator.ofInt(loadParams.height, maxHeight);
                    vaLoad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            loadParams.height = (Integer) vaLoad.getAnimatedValue();
                            load.setLayoutParams(loadParams);
                            invalidate();
                            fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
                        }
                    });
                    vaLoad.setDuration(200);
                    vaLoad.start();
                }
            } else if (load != null && load.getHeight() > 0 && load.getHeight() < dip2px(maxLoadHeight)) {
                //回到底部,load消失
                if (loadParams == null)
                    loadParams = (LinearLayout.LayoutParams) load.getLayoutParams();
                vaLoad = ValueAnimator.ofInt(loadParams.height, 0);
                vaLoad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        loadParams.height = (Integer) vaLoad.getAnimatedValue();
                        load.setLayoutParams(loadParams);
                        invalidate();
                    }
                });
                vaLoad.setDuration(200);
                vaLoad.start();
            }
        }
    }

    public void stopLoadMore(){
        if (load != null && loadParams != null) {
            isLoadMore = false;
            vaLoad = ValueAnimator.ofInt(loadParams.height, 0);
            vaLoad.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    loadParams.height = (Integer) vaLoad.getAnimatedValue();
                    load.setLayoutParams(loadParams);
                    invalidate();
                }
            });
            vaLoad.setDuration(200);
            vaLoad.start();
        }
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
        scrollY = y;
    }

    /**
     * 滑动监听器
     */
    public interface ScrollViewListener{

        void onScrollChanged(NestedScrollView scrollView, int x,
                             int y, int oldx, int oldy);

    }
    private ScrollViewListener scrollViewListener = null;
    /**
     * 设置监听器
     * @param scrollViewListener
     */
    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }


    private OnScrollBottomListener scrollBottomListener;
    /**
     * 设置监听器
     */
    public void setOnScrollBottomListener(OnScrollBottomListener scrollBottomListener) {
        this.scrollBottomListener = scrollBottomListener;
    }
    //加载回调的接口
    public interface OnScrollBottomListener {
        void loadData();
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dip2px(float dpValue) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, Resources.getSystem().getDisplayMetrics());
        return (int) px;
    }
}
