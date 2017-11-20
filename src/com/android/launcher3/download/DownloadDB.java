package com.android.launcher3.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.android.launcher3.LauncherApplication;

import java.io.File;
import java.util.ArrayList;

public class DownloadDB {
	
	private SQLiteDatabase mDatabase;
	private static DownloadDB instance;
	
	public static synchronized DownloadDB getInstance(){
		if (instance == null){
			instance = new DownloadDB();
		}
		return instance;
	}
	
	public synchronized SQLiteDatabase openDatabase() {
		mDatabase = new DownloadDBHelper(LauncherApplication.getInstance()).getWritableDatabase();
		return mDatabase;
	}
	
	//插入未完成的
	public synchronized void insertUnfinished(DownloadManager.DownloadTask info){
		int result = updateUnfinished(info);
		if (result == 0) {
			SQLiteDatabase db = openDatabase();
			ContentValues values = new ContentValues();
			values.put(DownloadDBHelper.COLUMN_ID, info.info.id);
			values.put(DownloadDBHelper.COLUMN_NAME, info.info.name);
			values.put(DownloadDBHelper.COLUMN_SIZE, info.info.size);
			values.put(DownloadDBHelper.COLUMN_CURRENTSIZE, info.compeleteSize);
			values.put(DownloadDBHelper.COLUMN_URL, info.info.url);
			values.put(DownloadDBHelper.COLUMN_START_POS, info.startPos);
			values.put(DownloadDBHelper.COLUMN_END_POS, info.endPos);
			values.put(DownloadDBHelper.COLUMN_THREAD_NAME, info.threadName);
			db.insert(DownloadDBHelper.TABLE_THEME_UNFINISHED, null, values);
			db.close();
		}
	}
	//更新未完成的
	public synchronized int updateUnfinished(DownloadManager.DownloadTask info){
		SQLiteDatabase db = openDatabase();
		ContentValues values = new ContentValues();
		values.put(DownloadDBHelper.COLUMN_CURRENTSIZE, info.compeleteSize);
		int result = db.update(DownloadDBHelper.TABLE_THEME_UNFINISHED, values, DownloadDBHelper.COLUMN_ID+"=? and " + DownloadDBHelper.COLUMN_THREAD_NAME+"=?", new String[]{info.info.id+"", info.threadName});
		db.close();
		return result;
	}
	//删除未完成的
	public synchronized void deleteUnfinished(String name){
		SQLiteDatabase db = openDatabase();
		db.delete(DownloadDBHelper.TABLE_THEME_UNFINISHED, DownloadDBHelper.COLUMN_NAME+"=?", new String[]{name});
		db.close();
	}

	//查询未完成的
	public ArrayList<DownloadTaskInfo> queryAllUnfinished(DownloadManager downloadManager){
		ArrayList<DownloadTaskInfo> list = new ArrayList<>();
        SparseArray<DownloadTaskInfo> array = new SparseArray<>();
		SQLiteDatabase db = openDatabase();
		Cursor cursor = db.query(DownloadDBHelper.TABLE_THEME_UNFINISHED, null, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0){
			while(cursor.moveToNext()){

				int id = (cursor.getInt(cursor.getColumnIndex(DownloadDBHelper.COLUMN_ID)));
                DownloadTaskInfo info = array.get(id);
                if (info == null) {
                    info = new DownloadTaskInfo();
                    info.id = id;
                    info.name = (cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_NAME)));
                    info.size = (Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_SIZE))));
                    info.url = (cursor.getString(cursor.getColumnIndex("url")));
                    info.downloadState = DownloadManager.STATE_PAUSED;
                    info.initState = 2;

                    array.put(info.id, info);
                }
                DownloadManager.DownloadTask task = downloadManager.new DownloadTask(
                        info
                        , cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_THREAD_NAME))
                        , Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_START_POS)))
                        , Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_END_POS)))
                        , Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_CURRENTSIZE)))
                );
                info.addCurrentSize(task.compeleteSize);
				info.oldDownloaded = info.getCurrentSize();
                info.taskLists.add(task);
			}
		}

        for(int i = 0, size = array.size(); i < size; i++) {
            list.add(array.valueAt(i));
        }
		closeCursor(cursor);
		db.close();
		return list;
	}
	
	//插入完成的
	public void insertFinished(DownloadTaskInfo info){
		SQLiteDatabase db = openDatabase();
		ContentValues values = new ContentValues();
		values.put(DownloadDBHelper.COLUMN_ID, info.id);
		values.put(DownloadDBHelper.COLUMN_NAME, info.name);
		values.put(DownloadDBHelper.COLUMN_SIZE, info.size);
		values.put(DownloadDBHelper.COLUMN_URL, info.url);
		db.insert(DownloadDBHelper.TABLE_THEME_FINISHED,null, values);
		db.close();
	}
	//删除完成的
	public synchronized void deleteFinished(String themename){
		SQLiteDatabase db = openDatabase();
		db.delete(DownloadDBHelper.TABLE_THEME_FINISHED, DownloadDBHelper.COLUMN_NAME+"=?", new String[]{themename});
		db.close();
	}
	//查询完成的
	public ArrayList<DownloadTaskInfo> queryAllFinished(){
		ArrayList<DownloadTaskInfo> list = new ArrayList<>();;
		SQLiteDatabase db = openDatabase();
		Cursor cursor = db.query(DownloadDBHelper.TABLE_THEME_FINISHED, null, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0){
			while(cursor.moveToNext()){
				DownloadTaskInfo info = new DownloadTaskInfo();
				info.id = cursor.getInt(cursor.getColumnIndex(DownloadDBHelper.COLUMN_ID));
				info.name = (cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_NAME)));
				info.size = (Long.valueOf(cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_SIZE))));
				info.url = (cursor.getString(cursor.getColumnIndex(DownloadDBHelper.COLUMN_URL)));
				info.downloadState = (DownloadManager.STATE_DOWNLOADED);
				info.setCurrentSize(info.size);
				if (new File(DownloadTaskInfo.getPath(info.name)).exists()) {
					list.add(info);
				}
			}
		}
		closeCursor(cursor);
		db.close();
		return list;
	}
	
	public static void closeCursor(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
}
