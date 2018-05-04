####Android全屏切换到非全屏界面的视觉问题
    1.客户端从闪屏页（全屏模式）进入app主页（有状态栏）时
    2.查看大图页面（全屏沉浸）返回上个页面（有状态栏）时
    
    finish();
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    Intent intent = new Intent();
    startActivity(intent);