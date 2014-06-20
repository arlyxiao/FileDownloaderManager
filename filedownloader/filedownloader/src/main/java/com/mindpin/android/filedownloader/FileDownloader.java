package com.mindpin.android.filedownloader;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.PendingIntent;
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


//    private void init_connection(Context context,
//                                 String download_url,
//                                 File file_save_dir,
//                                 int thread_num) {
//        try {
//            Log.i("下载的 URL ", download_url);
//            this.context = context;
//            this.download_url = download_url;
//            file_record = new FileRecord(this.context);
//            URL url = new URL(this.download_url);
//            if(!file_save_dir.exists()) file_save_dir.mkdirs();
//            this.threads = new DownloadThread[thread_num];
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setConnectTimeout(5*1000);
//            conn.setRequestMethod("GET");
//            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
//            conn.setRequestProperty("Accept-Language", "zh-CN");
//            conn.setRequestProperty("Referer", download_url);
//            conn.setRequestProperty("Charset", "UTF-8");
//            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
//            conn.setRequestProperty("Connection", "Keep-Alive");
//            conn.connect();
//            print_response_header(conn);
//            if (conn.getResponseCode()==200) {
//                this.file_size = conn.getContentLength();
//                Log.i("初始化连接 文件大小 ", Integer.toString(file_size));
//                if (this.file_size <= 0) throw new RuntimeException("Unkown file size ");
//
//                String filename = get_file_name();
//                this.save_file = new File(file_save_dir, filename);
//                Map<Integer, Integer> logdata = file_record.get_data(download_url);
//                if(logdata.size()>0){
//                    for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
//                        thread_data.put(entry.getKey(), entry.getValue());
//                }
//                if(this.thread_data.size()==this.threads.length){
//                    for (int i = 0; i < this.threads.length; i++) {
//                        this.downloaded_size += this.thread_data.get(i+1);
//                    }
//                    print("已经下载的长度 "+ this.downloaded_size);
//                }
//                //计算每条线程下载的数据长度
//                this.block = (this.file_size % this.threads.length)==0?
//                        this.file_size / this.threads.length :
//                        this.file_size / this.threads.length + 1;
//            }else{
//                throw new RuntimeException("server no response ");
//            }
//        } catch (Exception e) {
//            print(e.toString());
//            Log.i("下载错误 ", e.getMessage());
//            throw new RuntimeException("don't connection this url");
//        }
//    }


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
        FileDownloader.this.listener = listener;

        Intent download_service = new Intent(context, DownloadService.class);
        context.startService(download_service);
        context.bindService(download_service, mConnection, Context.BIND_AUTO_CREATE);
    }


    DownloadService m_service;
    boolean m_bound = false;


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
            m_service = binder.getService();
            try {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... objects) {
                        try {
                            m_service.download(current, current.listener);
                        } catch (Exception e) {
                            Log.i("下载有问题1 ", e.getMessage());
                        }


                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void obj) {
                        super.onPostExecute(obj);


                    }
                }.execute();

            } catch (Exception e) {
                Log.i("下载有问题 ", e.getMessage());
            }

            m_bound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            m_bound = false;
        }
    };
}