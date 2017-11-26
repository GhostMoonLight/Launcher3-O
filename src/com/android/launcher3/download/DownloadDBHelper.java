package com.android.launcher3.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 下载数据库
 */
class DownloadDBHelper extends SQLiteOpenHelper {

	static String COLUMN_ID = "id";
	static String COLUMN_NAME = "name";
	static String COLUMN_SIZE = "size";
	static String COLUMN_CURRENTSIZE = "currentsize";
	static String COLUMN_URL = "url";
	static String COLUMN_START_POS = "start_pos";  //下载开始位置
	static String COLUMN_END_POS = "edn_pos";      //下载结束的位置
	static String COLUMN_THREAD_NAME = "thread_name";
	
	static String TABLE_THEME_UNFINISHED = "download_unfinished";
	static String TABLE_THEME_FINISHED = "download_finished";
	private static int VERSION = 1;

	DownloadDBHelper(Context context) {
		super(context, "download.db", null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//下载未完成的表
		db.execSQL("create table "+TABLE_THEME_UNFINISHED+"(_id integer PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID + " integer, " +
                COLUMN_NAME + " char, " +
                COLUMN_SIZE +" char, " +
                COLUMN_CURRENTSIZE + " char," +
                COLUMN_URL + " char, " +
                COLUMN_START_POS + " char, " +
                COLUMN_END_POS + " char, " +
                COLUMN_THREAD_NAME + " char)");
		//下载完成的表
		db.execSQL("create table "+TABLE_THEME_FINISHED+"(_id integer PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID + " integer, " +
                COLUMN_NAME + " char, " +
                COLUMN_SIZE +" char, " +
                COLUMN_URL + " char)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
