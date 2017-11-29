###Launcher UI 分析：

#####fitSystemWindows属性： 
     SystemWindows 指的就是屏幕上status bar、 navigation bar等系统控件所占据的部分。
         android:fitsSystemWindows="true"，这个属性的作用就是通过设置View的padding，
         使得应用的content部分——Activity中setContentView()中传入的就是
         content——不会与system window重叠。fitsSystemWindows默认为false
         
     只有将statusbar设为透明，或者界面设为全屏显示
     （设置View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN flag)时，fitsSystemWindows才会起作用
     
     1.fitsSystemWindows 需要被设置给根View——这个属性可以被设置给任意View，
        但是只有根View（content部分的根）外面才是SystemWindow，所以只有设置给根View才有用。
     2.Insets始终相对于全屏幕——Insets即边框，它决定着整个Window的边界。对Insets设置padding的时候，
        这个padding始终是相对于全屏幕的。因为Insets的生成在View layout之前就已经完成了，
        所以系统对于View长什么样一无所知。
     3.其它padding将通通被覆盖。需要注意，如果你对一个View设置了android:fitsSystemWindows="true"，
        那么你对该View设置的其他padding将通通无效。
        

#####Launcher中fitSystemWindows的使用：
     launcher.xml中把跟布局LauncherRootView设置了属性fitSystemWindows为true
     protected boolean fitSystemWindows(Rect insets)该方法优先于onMeasure被调用
     
     LauncherRootView中的fitSystemWindows方法会遍历子View，让子View来根据Insets来布局，
     同时会回调Launcher中的onInsetsChanged方法，让Launcher重新布局
    