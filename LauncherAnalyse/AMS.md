##AMS

#####ActivityManagerService, ActivityManagerNative, ActivityManagerProxy, ActivityManager的关系
    1. ActivityManagerNative是一个抽象类继承Binder,实现IActivityManger接口
        public abstract class ActivityManagerNative extends Binder implements IActivityManager
        gDefault.get()返回的就是IActivityManager，如果实在客户端调用就是返回的ActivityManagerProxy对象。

    2. ActivityManagerProxy是ActivityManagerNative同文件的类,不是内部类,两个类都在同一个文件中, 
       实现IActivityManager接口

    3. ActivityManagerService继承了ActivityManagerNative类
        ActivityMangerProxy远程调用方法后，先到ActivityManagerService类中的onTransact方法,该方法中调用
        super.onTransact调用父类ActivityManagerNative的onTransact方法,最终都会调用ActivityManagerService
        中的方法
        ActivityManagerService的实例在进程SystemServer刚启动时初始化

    4. ActivityManager作为中介来访问IActivityManager提供的功能。
        ActivityManager是通过ActivityManagerNative.getDefault()来获取到IActivityManager这个接口的。
        因为ActivityManager是运行在用户进程的，因而getDefault()获取的是ActivityManagerProxy对象.
		
				

#####mAppThread是ApplicationThread对象
      ApplicationThread是ActivityThread的内部类，继承ApplicationThreadNative；
      ApplicationThreadNative抽象类继承Binder实现IApplicationThread；
      IApplicationThread中定义了AmS可以访问app的接口,AmS通过这些接口控制app进程以及完成app的响应.
    
      ApplicationThread是AMS和App通信的桥梁.
    
      ApplicationThread通过binder与Ams通信，并将Ams的调用，
      通过H类（也就是Hnalder）将消息发送到消息队列，然后进行相应的操作。
    
    
      AMS与UI线程通信的机制：通过ApplicationThread进行中转，再通过Handler转发到ActivityThread所在的线程中执行。
					