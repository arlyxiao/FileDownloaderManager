package com.mindpin.android.filedownloader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "download_filerecord.db";
    private static final int VERSION = 1;

    public DBOpenHelper(Context context) {

        super(context, DBNAME, null, VERSION);
        Log.i("调试db 0", "true");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("调试db 1", "true");
        db.execSQL("CREATE TABLE IF NOT EXISTS filedownlog (id integer primary key autoincrement, downpath varchar(100), threadid INTEGER, downlength INTEGER)");
        // 这里的 status 字段用来表示暂停或者正在下载, 暂停: pause, 正在下载: download
        db.execSQL("CREATE TABLE IF NOT EXISTS filesizelog (downpath varchar(100), filesize INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS filedownlog");
        db.execSQL("DROP TABLE IF EXISTS filesizelog");
        onCreate(db);
        Log.i("调试db 2", "true");
    }

}