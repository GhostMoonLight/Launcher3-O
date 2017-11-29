###事件处理是Launcher的核心功能之一
#####一. Launcher中的几个事件处理类：
     DragLayer:                    事件分发核心类
     DragController:               拖拽操作处理类
     AllAppsTransitionController:  AllApps界面滑动处理类
     WidgetBottomSheet:            在底部显示的Widget的滑动处理类
     AppWidgetResizeFrame:         调整Widget大小的处理类
     PinchToOverviewListener:      
     
     有关拖拽的监听回调：
     DragController.DragListener:
        onDragStart:  开始拖拽的回调  在DragController中的handleMoveEvent中触发
        onDragEnd:    拖拽结束的回调
        
     DropTarget:   
        isDropEnabled: 指定此放置目标是否当前已启用(用于暂时禁用某些放置目标)
        onDrop:        处理放在DropTarget上的对象(放置到该目标上)
        onDragEnter:   进入该DragTarget
        onDragOver:    在该DragTarget中移动
        onDragExit:    移除该DragTarget
        acceptDrop:    检查是否可以在请求的位置或附近发生放置操作。这将在onDrop之前调用。
        prepareAccessibilityDrop:
        getHitRectRelativeToDragLayer:
        
     DragSource:
        supportsAppInfoDropTarget:   从这个源拖动的项目是否支持“应用程序信息”
        supportsDeleteDropTarget:    从这个源拖动的项目是否支持“删除”放置目标
                                     如果返回false，则放置目标将显示“取消”而不是“移除”。
        getIntrinsicIconScaleFactor: 工作区图标大小上的图标比例
        onDropCompleted:             拖拽完成(来自此源的项目放到DropTarget后，回调源会被回调。)
        
        
#####拖拽时顶部显示 移除,应用信息,卸载 的layout：
     DropTargetBar继承LinearLayout，实现DragListener，用来显示ButtonDropTarget.
     移除,应用信息,卸载每一个名称都是ButtonDropTarget
        ButtonDropTarget继承TextView，实现DropTarget，DragListener
            DeleteDropTarget：   显示移除或者取消
            UninstallDropTarget：显示卸载
                InfoDropTarget： 继承UninstallDropTarget。显示应用信息
     
#####拖拽事件的流程
     长按事件都是在Launcher的onLongClick方法中触发的
     onLongClick事件中创建拖拽的图标
     DragLayer的onInterceptTouchEvent中进行事件的分发,
     判断是不是要拦截该事件交给相应的TouchController处理。
     然后再onTouchEvent中由TouchController处理事件
     
     ------------------------
     DragController中handleMoveEvent方法用来处理滑动事件，会触发callOnDragStart回调onDragStart方法,
     DropTargetBar中的onDragStart方法会让DropTargetBar显示出来;
     Workspace中的onDragStart方法中会让Workspace进入拖拽模式;
     


    