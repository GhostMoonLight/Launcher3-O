package com.android.launcher3;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;

import com.android.launcher3.logging.LogUtils;

public class LauncherRootView extends InsettableFrameLayout {

    private final Paint mOpaquePaint;
    @ViewDebug.ExportedProperty(category = "launcher")
    private boolean mDrawSideInsetBar;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mLeftInsetBarWidth;
    @ViewDebug.ExportedProperty(category = "launcher")
    private int mRightInsetBarWidth;

    private View mAlignedView;    // 实际就是DragLayer

    public LauncherRootView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mOpaquePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOpaquePaint.setColor(Color.BLACK);
        mOpaquePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 0) {
            // LauncherRootView contains only one child, which should be aligned
            // based on the horizontal insets.
            mAlignedView = getChildAt(0);
        }
        super.onFinishInflate();
    }

    @TargetApi(23)
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        LogUtils.eTag("insets:"+insets);
        if (insets.top == 0 && insets.bottom == 0){
            // 进入全屏模式的时候insets的top和bottom都为0
            // 对于有虚拟按键的设备来说bottom是不为0的，进入全屏模式的时候bottom就变为0了，
            // 进入全屏模式的时候会重新布局，bottom的值变化会影响布局
            // 这里如果top和bottom都为0则使用之前的mInsets中的值
            insets.top = mInsets.top;
            insets.bottom = mInsets.bottom;
        }
        boolean rawInsetsChanged = !mInsets.equals(insets);
        mDrawSideInsetBar = (insets.right > 0 || insets.left > 0) &&
                (!Utilities.ATLEAST_MARSHMALLOW ||
                getContext().getSystemService(ActivityManager.class).isLowRamDevice());
        mRightInsetBarWidth = insets.right;
        mLeftInsetBarWidth = insets.left;

        // 该方法中会遍历子View，让子View根据Insets来布局
        setInsets(mDrawSideInsetBar ? new Rect(0, insets.top, 0, insets.bottom) : insets);

        // 如果left和right不为0，则需要给DragLayout设置leftMargin和rightMargin
        if (mAlignedView != null && mDrawSideInsetBar) {
            // Apply margins on aligned view to handle left/right insets.
            MarginLayoutParams lp = (MarginLayoutParams) mAlignedView.getLayoutParams();
            if (lp.leftMargin != insets.left || lp.rightMargin != insets.right) {
                lp.leftMargin = insets.left;
                lp.rightMargin = insets.right;
                mAlignedView.setLayoutParams(lp);
            }
        }

        if (rawInsetsChanged) {
            // Update the grid again
            Launcher launcher = Launcher.getLauncher(getContext());
            // Insets变化的回调，通知Launcher重新布局
            launcher.onInsetsChanged(insets);
        }

        return true; // I'll take it from here
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // If the right inset is opaque, draw a black rectangle to ensure that is stays opaque.
        // 如果右边的插图是不透明的，画一个黑色的矩形以确保它保持不透明。
        if (mDrawSideInsetBar) {
            if (mRightInsetBarWidth > 0) {
                int width = getWidth();
                canvas.drawRect(width - mRightInsetBarWidth, 0, width, getHeight(), mOpaquePaint);
            }
            if (mLeftInsetBarWidth > 0) {
                canvas.drawRect(0, 0, mLeftInsetBarWidth, getHeight(), mOpaquePaint);
            }
        }
    }
}