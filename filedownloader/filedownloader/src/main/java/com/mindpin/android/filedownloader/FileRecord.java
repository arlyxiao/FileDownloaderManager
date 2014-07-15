package com.mindpin.android.filedownloader;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 业务bean
 *
 */
public class FileRecord {
    private DBOpenHelper db_open_helper;

    public FileRecord(Context context) {
        db_open_helper = new DBOpenHelper(context);
    }
    /**
     * 获取每条线程已经下载的文件长度
     * @param path
     * @return
     */
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
    /**
     * 保存每条线程已经下载的文件长度
     * @param path
     * @param map
     */
    public void save(String path,  Map<Integer, Integer> map){//int threadid, int position
        SQLiteDatabase db = db_open_helper.getWritableDatabase();
        db.beginTransaction();
        try{
            for(Map.Entry<Integer, Integer> entry : map.entrySet()){
                db.execSQL("insert into filedownlog(downpath, threadid, downlength) values(?,?,?)",
                        new Object[]{path, entry.getKey(), entry.getValue()});
            }
            db.setTransactionSuccessful();
        }finally{
            db.endTransaction();
        }
        db.close();
    }
    /**
     * 实时更新每条线程已经下载的文件长度
     * @param path
     * @param map
     */
    public void update(String path, Map<Integer, Integer> map){
        SQLiteDatabase db = db_open_helper.getWritableDatabase();
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
    /**
     * 当文件下载完成后，删除对应的下载记录
     * @param path
     */
    public void delete(String path){
        try {
            SQLiteDatabase db = db_open_helper.getWritableDatabase();
            db.execSQL("delete from filedownlog where downpath=?", new Object[]{path});
            db.close();
        } catch (Exception e) {
            Log.i("数据库删除错误 ", e.toString());
            e.printStackTrace();
        }

    }

}