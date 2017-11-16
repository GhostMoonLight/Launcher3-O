package com.android.launcher3.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class DownloadManager {
	public static final int STATE_NONE = 0;
	/** 等待中 */
	public static final int STATE_WAITING = 1;
	/** 下载中 */
	public static final int STATE_DOWNLOADING = 2;
	/** 暂停 */
	public static final int STATE_PAUSED = 3;
	/** 下载完毕 */
	public static final int STATE_DOWNLOADED = 4;
	/** 下载失败 */
	public static final int STATE_ERROR = 5;

	private static DownloadManager instance;

	private DownloadManager() {
        //初始化下载任务列表
        ArrayList<DownloadTaskInfo> list = DownloadDB.getInstance().queryAllUnfinished(this);
        for (DownloadTaskInfo i: list){
            addDownloadInfo(i);
        }

        list = DownloadDB.getInstance().queryAllFinished();
        for (DownloadTaskInfo i: list){
            addDownloadInfo(i);
        }
	}

	private Map<Integer, DownloadTaskInfo> mDownloadMap = new ConcurrentHashMap<>();
    private Map<Integer, InitDownloadTask> mInitTaskMap = new ConcurrentHashMap<>();
	/** 用于记录观察者，当信息发送了改变，需要通知他们 */
	private final List<DownloadObserver> mObservers = new ArrayList<>();
    private static final int THREAD_COUNT = 3;

	public static synchronized DownloadManager getInstance() {
		if (instance == null) {
			instance = new DownloadManager();
		}
		return instance;
	}
	
	//获取下载任务
	public Map<Integer, DownloadTaskInfo> getDownloadMap(){
		return mDownloadMap;
	}
	//添加下载任务
	public void addDownloadInfo(DownloadTaskInfo info){
		DownloadTaskInfo i = mDownloadMap.get(info.getId());
		if (i == null) {
			mDownloadMap.put(info.getId(), info);
		}
	}

	/** 注册观察者 */
	public void registerObserver(DownloadObserver observer) {
		synchronized (mObservers) {
			if (!mObservers.contains(observer)) {
				mObservers.add(observer);
			}
		}
	}

	/** 反注册观察者 */
	public void unRegisterObserver(DownloadObserver observer) {
		synchronized (mObservers) {
			if (mObservers.contains(observer)) {
				mObservers.remove(observer);
			}
		}
	}

	/** 当下载状态发送改变的时候回调 */
	public void notifyDownloadStateChanged(DownloadTaskInfo info) {
	    if (info.isInvalid) return; // 如果该任务无效就不回调
		synchronized (mObservers) {
			for (DownloadObserver observer : mObservers) {
				observer.onDownloadStateChanged(info);
			}
		}
	}

	/** 当下载进度发送改变的时候回调 */
	public void notifyDownloadProgressed(DownloadTaskInfo info) {
		synchronized (mObservers) {
			for (DownloadObserver observer : mObservers) {
				observer.onDownloadProgressed(info);
			}
		}
	}

	/** 下载，需要传入一个DownloadInfo对象 */
	public synchronized void download(DownloadInfo appInfo) {
		//先判断是否有这个app的下载信息
		DownloadTaskInfo info = mDownloadMap.get(appInfo.id);
		if (info == null) {//如果没有，则根据appInfo创建一个新的下载信息
			info = DownloadTaskInfo.clone(appInfo);
			mDownloadMap.put(appInfo.id, info);
		}else{
            //如果下载任务存在，且状态是暂停，继续下载
            if (info.getDownloadState() == STATE_PAUSED && info.initState != 0) {
                if (info.initState == 2 || info.initState == 3){
                    info.setDownloadState(STATE_DOWNLOADING);//先改变下载状态
                    notifyDownloadStateChanged(info);
                    executeDownload(info);
                } else if (info.initState == 1){
                    info.setDownloadState(STATE_WAITING);
                } else {
                    //为0说明当前线程池已满，等待执行中被暂停了,初始化任务还没执行
					System.out.print("");
                }
                notifyDownloadStateChanged(info);
                return;
            }
        }

		//判断状态是否为STATE_NONE、STATE_PAUSED、STATE_ERROR。只有这3种状态才能进行下载，其他状态不予处理
		if (info.getDownloadState() == STATE_NONE || info.getDownloadState() == STATE_PAUSED || info.getDownloadState() == STATE_ERROR) {
			//下载之前，把状态设置为STATE_WAITING，因为此时并没有产开始下载，只是把任务放入了线程池中，当任务真正开始执行时，才会改为STATE_DOWNLOADING
			info.setDownloadState(STATE_WAITING);
			notifyDownloadStateChanged(info);//每次状态发生改变，都需要回调该方法通知所有观察者
            InitDownloadTask task = new InitDownloadTask(info);//创建一个下载任务，放入线程池
            mInitTaskMap.put(info.getId(), task);
            ThreadManager.getDownloadPool().execute(task);
		}
	}

    private void continueTask(DownloadTaskInfo info){
        info.setDownloadState(STATE_DOWNLOADING);
        notifyDownloadStateChanged(info);
        synchronized (info) {
            info.notifyAll();
        }
    }

	/** 暂停下载 */
	public synchronized void pause(DownloadInfo appInfo) {
//		stopDownload(appInfo);
		DownloadTaskInfo info = mDownloadMap.get(appInfo.id);//找出下载信息
		if (info != null) {//修改下载状态
			info.setDownloadState(STATE_PAUSED);
			notifyDownloadStateChanged(info);
            if (info.initState == 0){
                InitDownloadTask initTask = mInitTaskMap.remove(info.getId());
                ThreadManager.getDownloadPool().cancel(initTask);
            } else if (info.initState == 3){
                // 抛弃之前的DownloadTaskInfo，put一个新的DownloadTaskInfo的对象
                mDownloadMap.put(appInfo.id, info.cloneSelf());
                info.isInvalid = true; //老的任务设置成无效的，之后该DownloadTaskInfo就不会刷新回调
            }
		}
	}

	/** 取消下载，逻辑和暂停类似，只是需要删除已下载的文件 */
	public synchronized void cancel(DownloadInfo appInfo) {
        DownloadTaskInfo info = mDownloadMap.remove(appInfo.id);
		if (info != null) {//修改下载状态并删除文件
            for (DownloadTask task: info.taskLists){
                task.stopTask();
                ThreadManager.getDownloadPool().cancel(task);
            }
            info.setDownloadState(STATE_NONE);
            info.setCurrentSize(0);
			notifyDownloadStateChanged(info);
			File file = new File(info.getPath());
			file.delete();
            DownloadDB.getInstance().deleteUnfinished(info.name);
		}
	}

	/** 获取下载信息 */
	public synchronized DownloadTaskInfo getDownloadInfo(int id) {
		
		DownloadTaskInfo tInfo = mDownloadMap.get(id);
		
		if (tInfo != null){
			//判断下载的文件是否还存在
			File f = new File(tInfo.getPath());
			if (!f.exists()){
				mDownloadMap.remove(id);
				tInfo = null;
			}
		}
		
		return tInfo;
	}
	/** 获取全部下载中的信息 */
	public synchronized List<DownloadTaskInfo> getAllDownloadingInfo() {
		List<DownloadTaskInfo> all=new ArrayList<>();
		List<Integer> allId=new ArrayList<>();
		for (Entry<Integer, DownloadTaskInfo> entry : mDownloadMap.entrySet()) {
		    System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
		    if(entry.getValue().getDownloadState()!=STATE_DOWNLOADED){
		    	int downloadId=entry.getValue().getId();
		    	boolean needAdd=true;
		    	for(int i=0;i<allId.size();i++){
		    		if(downloadId==allId.get(i)){
		    			needAdd=false;
		    			break;
		    		}
		    	}
				if (needAdd) {
					all.add(entry.getValue());
					allId.add(entry.getValue().getId());
				}
		    }
		    
		}  
		return all;
	}

    /**
     * 初始化下载任务
     */
    public class InitDownloadTask implements Runnable {
        private DownloadTaskInfo info;

        public InitDownloadTask(DownloadTaskInfo info){
            this.info = info;
        }

        @Override
        public void run() {
            info.initState = 1;
            boolean ret;
            try {
                URL url = new URL(info.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(1000);
                connection.setRequestMethod("GET");
                info.size = connection.getContentLength();
                if (info.size >= 0) {
                    ret = true;
                    File file = new File(info.getPath());
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    // 本地访问文件
                    RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
                    accessFile.setLength(info.size);
                    accessFile.close();
                    connection.disconnect();
                }else{
                    mDownloadMap.remove(info.getId());
                    ret = false;
                    info.setDownloadState(STATE_ERROR);
                    notifyDownloadStateChanged(info);
                }
            } catch (Exception e) {
                mDownloadMap.remove(info.getId());
                ret = false;
                info.setDownloadState(STATE_ERROR);
                notifyDownloadStateChanged(info);
                e.printStackTrace();
                try {
                    File file = new File(info.getPath());
                    if (!file.exists()) {
                        file.delete();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            mInitTaskMap.remove(info.getId());
            synchronized (info) {
                if (ret) {
                    int threadCount = THREAD_COUNT;
                    if (info.downloadState == STATE_PAUSED || info.downloadState == STATE_WAITING) {
                        long range = info.size / threadCount;
                        info.taskLists.clear();
                        for (int i = 0; i < threadCount - 1; i++) {
                            DownloadTask task = new DownloadTask(info, info.id + "_" + i, i * range, (i + 1)
                                    * range - 1, 0);
                            info.taskLists.add(task);
                        }
                        DownloadTask task = new DownloadTask(info, info.id + "_" + (threadCount - 1),
                                (threadCount - 1) * range, info.size - 1, 0);
                        info.taskLists.add(task);

                        info.initState = 2;
                        //开始下载
                        DownloadDB.getInstance().deleteFinished(info.name);
                        DownloadDB.getInstance().deleteUnfinished(info.name);
                        if (info.downloadState != STATE_PAUSED) {
                            executeDownload(info);
                        }
                    }
                }
            }
        }
    }

    private void executeDownload(DownloadTaskInfo info){
        for (DownloadTask downloadTask : info.taskLists) {
            DownloadDB.getInstance().insertUnfinished(downloadTask);
			downloadTask.isStop = false;
            ThreadManager.getDownloadPool().execute(downloadTask);
        }
        info.initState = 3;
    }

	/** 下载任务 */
	public class DownloadTask implements Runnable {
		public final DownloadTaskInfo info;
        public boolean isStop = false;
        public long startPos;
        public long endPos;
        public String threadName;
        public long compeleteSize;  //已经下载的长度
		public boolean isFinished; //是否执行结束

        private DownloadTask(DownloadTaskInfo info){
            this.info = info;
        }

		public DownloadTask(DownloadTaskInfo info, String threadName, long startPos, long endPos, long compeleteSize) {
			this.info = info;
            this.threadName = threadName;
            this.startPos = startPos;
            this.endPos = endPos;
            this.compeleteSize = compeleteSize;
		}
		
		public void stopTask(){
			isStop = true;
		}
		
		@Override
		public void run() {
            if (info.downloadState == STATE_PAUSED) {
                return;
            }
            if (info.downloadState != STATE_DOWNLOADING) {
                info.setDownloadState(STATE_DOWNLOADING);//先改变下载状态
                notifyDownloadStateChanged(info);
            }
            RandomAccessFile randomAccessFile = null;
			HttpURLConnection conn = null;
			InputStream stream = null;
			try {
                URL url = new URL(info.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                // 设置范围，格式为Range：bytes x-y;
                conn.setRequestProperty("Range", "bytes="
                        + (startPos + compeleteSize) + "-" + endPos);

                randomAccessFile = new RandomAccessFile(info.getPath(), "rwd");
                randomAccessFile.seek(startPos + compeleteSize);
				long currentTime;

				if ((stream = conn.getInputStream()) == null) {
					info.setDownloadState(STATE_ERROR);//没有下载内容返回，修改为错误状态
					notifyDownloadStateChanged(info);
				} else {

                    if (isStop || info.downloadState != STATE_DOWNLOADING) return;

					int count;
					
					byte[] buffer = new byte[1024*100];
                    boolean downloading = true;
					a:while (!isStop && downloading) {
						synchronized (info) {
							if (info.getDownloadState() == STATE_PAUSED){
								break a;
							}
						}
						
						//每次读取到数据后，都需要判断是否为下载状态，如果不是，下载需要终止; 如果是，则刷新进度
						if ((count = stream.read(buffer)) > 0){
                            randomAccessFile.write(buffer, 0, count);
                            synchronized (info) {
                                compeleteSize += count;
                                info.addCurrentSize(count);
                                currentTime = System.currentTimeMillis();
                                if (currentTime - info.lastUpdateTime > 1000) {
                                    notifyDownloadProgressed(info);//刷新进度
                                    info.lastUpdateTime = currentTime;
                                    info.setSpeed(info.getCurrentSize() - info.oldDownloaded);
                                    info.oldDownloaded = info.getCurrentSize();
                                }
								DownloadDB.getInstance().updateUnfinished(this);
                            }
						}else{
							downloading = false;
							synchronized (info) {
								info.setCompleteThreadCount();
								if (info.getCompleteThreadCount() == THREAD_COUNT) {
									info.setSpeed(0);
									if (checkDownloadFile(info.getPath())) {
										info.setDownloadState(STATE_DOWNLOADED);
										notifyDownloadStateChanged(info);
										DownloadDB.getInstance().insertFinished(info);
										DownloadDB.getInstance().deleteUnfinished(info.name);
									} else {
										info.setDownloadState(STATE_ERROR);
										notifyDownloadStateChanged(info);
										DownloadDB.getInstance().deleteUnfinished(info.name);
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				//出异常后需要修改状态并
				synchronized (info){
					for (DownloadTask task : info.taskLists) {
						task.stopTask();
					}
					if (info.downloadState == STATE_PAUSED){
						synchronized (info) {
							info.notifyAll();
						}
					}
					info.initState = 2;
					info.setDownloadState(STATE_PAUSED);
					notifyDownloadStateChanged(info);
					info.setSpeed(0);
				}
			} finally {
				if (randomAccessFile != null) {
					try {
                        randomAccessFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
                if (conn != null)
                    conn.disconnect();
			}
		}

        public DownloadTask cloneSelf(DownloadTaskInfo info) {
            DownloadTask t = new DownloadTask(info);
            t.threadName = threadName;
            t.startPos = startPos;
            t.endPos = endPos;
            t.compeleteSize = compeleteSize;
            info.addCurrentSize(compeleteSize);
            return t;
        }
    }
	
	/**
	 * 检查文件是否下载完成
	 */
	private boolean checkDownloadFile(String path) {
//		PackageUtil.AppSnippet sAppSnippet = PackageUtil.getAppSnippet(LauncherApplication.getInstance(), path);
//
//		if(sAppSnippet == null || TextUtils.isEmpty(sAppSnippet.packageName)) {
//			return false;
//		}
		return true;
	}

	public interface DownloadObserver {

        void onDownloadStateChanged(DownloadTaskInfo info);

        void onDownloadProgressed(DownloadTaskInfo info);
	}
}
