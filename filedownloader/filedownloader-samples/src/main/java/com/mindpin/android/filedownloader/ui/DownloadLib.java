package com.mindpin.android.filedownloader.ui;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileWriter;
import java.util.UUID;


@SuppressLint("NewApi")
public class DownloadLib {
    public static final Uri CONTENT_URI   = Uri.parse("content://downloads/my_downloads");
    DownloadManager downloadmanager;
    DownloadManager.Request request;
    DownloadChangeObserver download_observer;
    Handler handler;
    Context context;
    String download_url;
    File file_save_dir;
    Long download_id;
    String full_file_path;

    Uri uri;
    int filesize;

    Class activity_class;
    Bundle intent_extras;

    UpdateListener listener;
    boolean stop_download = false;

    public DownloadLib(Context context, String download_url, File file_save_dir) {
        this.context = context;
        this.download_url = download_url;
        this.file_save_dir = file_save_dir;

        handler = new MyHandler();
        downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        download_observer = new DownloadChangeObserver();

        // 设置下载目录，文件名
        String dir = file_save_dir.getPath();
        String name = get_filename();

        if (!create_dir(file_save_dir)) return;

        Log.i("测试dir ", dir);
        Log.i("测试name ", name);
        full_file_path = dir + "/" + name;


        // 初始化下载 URL 路径
        uri = Uri.parse(download_url);

        request = new DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(dir, name);

        // 设置只允许在WIFI的网络下下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        // 加入下载队列, 开始下载
        download_id = downloadmanager.enqueue(request);

        context.getContentResolver().registerContentObserver(
                CONTENT_URI, true, download_observer);
    }


    public void download(UpdateListener listener) {

        this.listener = listener;


        // 激活通知栏点击事件
        context.registerReceiver(on_notification_click,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));


        // 下载完成后事件
        context.registerReceiver(on_complete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }


    public String get_filename() {
        String filename = this.download_url.substring(download_url.lastIndexOf('/') + 1);
        if(filename==null || "".equals(filename.trim())){
            filename = UUID.randomUUID()+ ".tmp";
        }

        return filename;
    }


    public void remove_download() {
        downloadmanager.remove(download_id);

        stop_download = true;
    }


    public void set_notification(Class activity_class, Bundle intent_extras) {
        this.activity_class = activity_class;
        this.intent_extras = intent_extras;
    }

    private boolean create_dir(File file_dir) {
        file_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + file_dir.getPath());
        if (file_dir.exists()) {
            Log.i("目录已经存在 ", file_dir.getAbsolutePath());
            return true;
        }

        Log.i("目录不存在 开始创建目录 ", "true");
        boolean result = false;

        try{
            file_dir.mkdir();
            Log.i("目录创建成功 ", "true");
            return true;
        } catch(SecurityException se){
            Log.i("目录创建失败 ", se.toString());
        }

        return false;
    }


    // 通知栏点击逻辑事件处理
    BroadcastReceiver on_notification_click = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(ctxt, activity_class);
            i.putExtras(intent_extras);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctxt.startActivity(i);
        }
    };


    // 完成下载后通知栏逻辑
    BroadcastReceiver on_complete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (stop_download) {
                Log.i("主动停止下载 不需要打开文件 ", "true");
                context.unregisterReceiver(on_complete);
                return;
            }
            open_file();
            context.unregisterReceiver(on_complete);
        }
    };


    protected void open_file() {
        Log.i("要打开的文件 ", full_file_path);
        File file = new File(full_file_path);

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            // intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), get_mime_type(file.getAbsolutePath()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Log.i("文件打开错误 ", e.getMessage());
        }
    }

    private String get_mime_type(String url) {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }



    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {

            int downloaded_size = get_downloaded_size();
            filesize = get_filesize();

            Log.i("已经下载的大小 ", Integer.toString(downloaded_size));
            handler.sendMessage(handler.obtainMessage(0, downloaded_size, filesize));


        }

    }


    private int[] get_bytes_and_status() {
        int[] bytes_and_status = new int[] {-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(download_id);
        Cursor c = null;
        try {
            c = downloadmanager.query(query);
            if (c != null && c.moveToFirst()) {
                bytes_and_status[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                Log.i("到目前为止下载的大小 ", Integer.toString(bytes_and_status[0]));
                bytes_and_status[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                Log.i("总大小 ", Integer.toString(bytes_and_status[0]));
                bytes_and_status[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.i("下载状态 ", Integer.toString(bytes_and_status[2]));
            }
            return bytes_and_status;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }


    private int get_downloaded_size() {
        int[] bytes_and_status = get_bytes_and_status();
        return bytes_and_status[0];
    }

    public int get_filesize() {
        int size = -1;

        while(size <= 0) {
            if (stop_download) return size;
            int[] bytes_and_status = get_bytes_and_status();

            Log.i("内部总大小 ", Integer.toString(bytes_and_status[1]));
            size = bytes_and_status[1];
        }

        return size;



//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                try {
//                    URL url = new URL(download_url);
//                    URLConnection urlConnection = url.openConnection();
//                    urlConnection.connect();
//                    filesize = urlConnection.getContentLength();
//                } catch (Exception e) {
//                    Log.i("获取 filesize 错误 ", e.toString());
//                }
//            }
//        }).start();
//
//        return filesize;
    }


    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Log.i("第二个参数值 ", Integer.toString(msg.arg1));

                DownloadLib.this.listener.on_update(msg.arg1);
            } catch (Exception e) {

            }

        }
    }




}
