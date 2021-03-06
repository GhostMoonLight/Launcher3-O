/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.dragndrop;

import android.content.Context;
import android.view.DragEvent;
import android.view.MotionEvent;

import com.android.launcher3.DropTarget.DragObject;
import com.android.launcher3.Utilities;

/**
 * Base class for driving a drag/drop operation.
 * 用于拖动拖放操作的基类。
 * 拖拽操作的驱动类，用来判断是否拦截本次拖拽的触摸事件，并驱动其他类进行相应的处理
 * 通过EventListener回调驱动其他的类进行相应的操作
 * DragController是EventListener的唯一实现类，这里就是驱动DragController。
 */
public abstract class DragDriver {
    protected final EventListener mEventListener;    //该对象的实现为DragController

    public interface EventListener {
        void onDriverDragMove(float x, float y);
        void onDriverDragExitWindow();
        void onDriverDragEnd(float x, float y);
        void onDriverDragCancel();
    }

    public DragDriver(EventListener eventListener) {
        mEventListener = eventListener;
    }

    /**
     * Handles ending of the DragView animation.
     */
    public void onDragViewAnimationEnd() { }

    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mEventListener.onDriverDragMove(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_UP:
                mEventListener.onDriverDragMove(ev.getX(), ev.getY());
                mEventListener.onDriverDragEnd(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                mEventListener.onDriverDragCancel();
                break;
        }

        return true;
    }

    public abstract boolean onDragEvent (DragEvent event);


    /**
     * 是否拦截本次触摸事件
     * 这个方法直接返回true，既默认是拦截触摸事件的
     * @return true  拦截
     *         false 不拦截
     */
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_UP:
                mEventListener.onDriverDragEnd(ev.getX(), ev.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                mEventListener.onDriverDragCancel();
                break;
        }

        return true;
    }

    public static DragDriver create(Context context, DragController dragController,
            DragObject dragObject, DragOptions options) {
        if (Utilities.ATLEAST_NOUGAT && options.systemDndStartPoint != null) {
            return new SystemDragDriver(dragController, context, dragObject);
        } else {
            return new InternalDragDriver(dragController);
        }
    }
}

/**
 * Class for driving a system (i.e. framework) drag/drop operation.
 * 该类不拦截触摸操作,因为重写了父类的onInterceptTouchEvent方法，并直接放回false
 */
class SystemDragDriver extends DragDriver {

    private final DragObject mDragObject;
    private final Context mContext;

    float mLastX = 0;
    float mLastY = 0;

    public SystemDragDriver(DragController dragController, Context context, DragObject dragObject) {
        super(dragController);
        mDragObject = dragObject;
        mContext = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onDragEvent (DragEvent event) {
        final int action = event.getAction();

        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                mLastX = event.getX();
                mLastY = event.getY();
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                mLastX = event.getX();
                mLastY = event.getY();
                mEventListener.onDriverDragMove(event.getX(), event.getY());
                return true;

            case DragEvent.ACTION_DROP:
                mLastX = event.getX();
                mLastY = event.getY();
                mEventListener.onDriverDragMove(event.getX(), event.getY());
                mEventListener.onDriverDragEnd(mLastX, mLastY);
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                mEventListener.onDriverDragExitWindow();
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                mEventListener.onDriverDragCancel();
                return true;

            default:
                return false;
        }
    }
}

/**
 * Class for driving an internal (i.e. not using framework) drag/drop operation.
 * 该类因为没有重写父类的onInterceptTouchEvent方法，所以是拦截触摸操作的
 */
class InternalDragDriver extends DragDriver {
    public InternalDragDriver(DragController dragController) {
        super(dragController);
    }

    @Override
    public boolean onDragEvent (DragEvent event) {
        return false;
    }
};
