package com.mindpin.android.filedownloader;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class FileDownloader {
    public HttpURLConnection conn;
    public static final String TAG = "FileDownloader";
    public Context context;
    public FileRecord file_record;
    /* 已下载文件长度 */
    public int downloaded_size = 0;
    /* 原始文件长度 */
    public int file_size = 0;

    /* 线程数 */
    public DownloadThread[] threads;
    public int thread_num;

    /* 本地保存文件 */
    public File save_file;
    /* 缓存各线程下载的长度*/
    public Map<Integer, Integer> thread_data = new ConcurrentHashMap<Integer, Integer>();
    /* 每条线程下载的长度 */
    public int block;
    /* 下载路径  */
    public String download_url;

    Class activity_class;
    Bundle intent_extras;

    ProgressUpdateListener listener;

    public FileDownloader current = this;

    Intent download_service;



    public int get_thread_size() {
        return threads.length;
    }


    public int get_file_size() {
        return file_size;
    }


    protected synchronized void append(int size) {
        downloaded_size += size;
    }

    protected synchronized void update(int thread_id, int pos) {
        this.thread_data.put(thread_id, pos);
        this.file_record.update(this.download_url, this.thread_data);
    }


    public FileDownloader(Context context,
                          String download_url,
                          File file_save_dir,
                          int thread_num) {


        this.context = context;
        this.download_url = download_url;
        this.save_file = file_save_dir;
        this.thread_num = thread_num;
    }


    public void set_notification(Class activity_class, Bundle intent_extras) {
        this.activity_class = activity_class;
        this.intent_extras = intent_extras;
    }


    public String get_file_name() {
        String filename = this.download_url.substring(this.download_url.lastIndexOf('/') + 1);
        if(filename==null || "".equals(filename.trim())){
            for (int i = 0;; i++) {
                String mine = conn.getHeaderField(i);
                if (mine == null) break;
                if("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if(m.find()) return m.group(1);
                }
            }
            filename = UUID.randomUUID()+ ".tmp";
        }

        return filename;
    }





    public static Map<String, String> get_http_response_header(HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null) break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }


    public static void print_response_header(HttpURLConnection http){
        Map<String, String> header = get_http_response_header(http);
        for(Map.Entry<String, String> entry : header.entrySet()){
            String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
            print(key+ entry.getValue());
        }
    }

    public static void print(String msg){
        Log.i(TAG, msg);
    }



    public void download(final ProgressUpdateListener listener) throws Exception{
        this.listener = listener;

        download_service = new Intent(context, DownloadService.class);
        context.startService(download_service);
        context.bindService(download_service, m_connection, Context.BIND_AUTO_CREATE);

    }

    public void pause_download() {
        try {
            context.unbindService(m_connection);
        } catch (Exception e) {
            Log.i("绑定已经解除了 ", e.toString());
        }

        // context.stopService(download_service);
        Log.i("暂停下载 ", "true");
    }


    DownloadService m_service;
    boolean m_bound = false;


    private ServiceConnection m_connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            m_service = binder.getService();
            try {
                m_service.download(current, current.listener);
            } catch (Exception e) {
                Log.i("下载有问题14243 ", e.toString());
            }

            m_bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            m_bound = false;
        }
    };
}