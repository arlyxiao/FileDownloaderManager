package com.mindpin.android.filedownloader;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;


public class FileRecord {
    private DBOpenHelper db_open_helper;

    public FileRecord(Context context) {
        Log.i("调试db ", "true");
        db_open_helper = new DBOpenHelper(context);
    }

    public int get_downloaded_size(String path){
        int downloaded_size = 0;
        SQLiteDatabase db = db_open_helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select sum(downlength) from filedownlog where downpath=?", new String[]{path});
        if(cursor.moveToFirst()) {
            downloaded_size = cursor.getInt(0);
        }
        cursor.close();
        db.close();

        Log.i("数据库中的 downloaded_size ", Integer.toString(downloaded_size));


//        Map<Integer, Integer> logdata = get_data(path);
//        if(logdata.size()>0){
//            for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
//                downloaded_size += entry.getValue();
//        }
//
//        Log.i("数据库中的 downloaded_size ", Integer.toString(downloaded_size));

        return downloaded_size;
    }

    public void save_filezie(String path, int filesize) {
        SQLiteDatabase db;
        try {
            db = db_open_helper.getWritableDatabase();
        } catch (Exception e) {
            Log.i("getWritableDatabase 错误 ", e.toString());
            e.printStackTrace();
            return;
        }

        if (db == null) {
            return;
        }

        db.beginTransaction();
        try{
            Cursor cursor = db.rawQuery("select filesize from filesizelog where downpath=?", new String[]{path});
            if (cursor.getCount() > 0) {
                db.execSQL("update filesizelog set filesize=? where downpath=?",
                        new Object[]{filesize, path});
                Log.i("更新 filesize ", Integer.toString(filesize));
            } else {
                db.execSQL("insert into filesizelog(downpath, filesize) values(?,?)",
                        new Object[]{path, filesize});

                Log.i("保存 filesize ", Integer.toString(filesize));
            }

            db.setTransactionSuccessful();
            Log.i("更新 filesize ", "true");
        }finally{
            db.endTransaction();
        }
        db.close();
    }

    public int get_filesize(String path){
        int filesize = 0;
        SQLiteDatabase db = db_open_helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select filesize from filesizelog where downpath=?", new String[]{path});
        if(cursor.moveToFirst()) {
            filesize = cursor.getInt(0);
        }
        Log.i("filerecord 获取 filesize ", Integer.toString(filesize));
        cursor.close();
        db.close();
        return filesize;
    }


    public Map<Integer, Integer> get_data(String path){
        SQLiteDatabase db = db_open_helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select threadid, downlength from filedownlog where downpath=?", new String[]{path});
        Map<Integer, Integer> data = new HashMap<Integer, Integer>();
        while(cursor.moveToNext()){
            data.put(cursor.getInt(0), cursor.getInt(1));
        }
        cursor.close();
        db.close();
        return data;
    }


    public void save(String path,  Map<Integer, Integer> map){//int threadid, int position

        Log.i("save运行过 ", "true");

        int downlength;
        int threadid;

        SQLiteDatabase db = db_open_helper.getWritableDatabase();
        db.beginTransaction();
        try{




            for(Map.Entry<Integer, Integer> entry : map.entrySet()){

                downlength = entry.getValue();
                threadid = entry.getKey();

                Log.i("downlength值 ", Integer.toString(downlength));


//                Cursor cursor = db.rawQuery("select threadid from filedownlog where downpath=? and threadid=?",
//                        new String[]{path, String.valueOf(threadid)});
//                if (cursor.getCount() > 0) {
//                    Log.i("退出for ", Integer.toString(downlength));
//                    continue;
//                }


                db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
                        new Object[]{path, threadid, downlength});


            }

            db.setTransactionSuccessful();
        }finally{
            db.endTransaction();
        }
        db.close();
    }




    public void update(String path, Map<Integer, Integer> map){
        SQLiteDatabase db;
        try {
            db = db_open_helper.getWritableDatabase();
        } catch (Exception e) {
        // } catch (android.database.sqlite.SQLiteDatabaseLockedException e) {
            Log.i("getWritableDatabase 错误 ", e.toString());
            e.printStackTrace();
            return;
        }

//        db = db_open_helper.getWritableDatabase();

        if (db == null) {
            return;
        }

        db.beginTransaction();
        try{
            for(Map.Entry<Integer, Integer> entry : map.entrySet()){
                db.execSQL("update filedownlog set downlength=? where downpath=? and threadid=?",
                        new Object[]{entry.getValue(), path, entry.getKey()});
            }
            db.setTransactionSuccessful();
        }finally{
            db.endTransaction();
        }
        db.close();
    }



    public void delete(String path){
        try {
            SQLiteDatabase db = db_open_helper.getWritableDatabase();
            db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
            db.execSQL("delete from filesizelog where downpath=?", new Object[]{path});
            db.close();
        } catch (Exception e) {
            Log.i("数据库删除错误 ", e.toString());
            e.printStackTrace();
        }

    }

}