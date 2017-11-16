###事件处理是Launcher的核心功能之一
#####一. Launcher中的几个事件处理类：
     DragLayer:                    事件分发核心类
     v
     DragController:               拖拽操作处理类
     AllAppsTransitionController:  AllApps界面滑动处理类
     WidgetBottomSheet:            在底部显示的Widget的滑动处理类
     AppWidgetResizeFrame:         调整Widget大小的处理类
     PinchToOverviewListener:      
     
#####拖拽事件的流程
    长按事件都是在Launcher的onLongClick方法中触发的
    onLongClick事件中创建拖拽的图标
    DragLayer的onInterceptTouchEvent中进行事件的分发,
    判断是不是要拦截该事件交给相应的TouchController处理。
    然后再onTouchEvent中由TouchController处理事件


    