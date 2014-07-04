package com.mindpin.android.filedownloader.ui;


import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mindpin.android.filedownloader.FileDownloader;
import com.mindpin.android.filedownloader.R;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

    Uri uri;
    int filesize;

    Class activity_class;
    Bundle intent_extras;

    UpdateListener listener;

    public DownloadLib(Context context, String download_url, File file_save_dir) {
        this.context = context;
        this.download_url = download_url;
        this.file_save_dir = file_save_dir;

        handler = new MyHandler();
        downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        download_observer = new DownloadChangeObserver();


        // 初始化下载 URL 路径
        uri = Uri.parse(download_url);

        request = new DownloadManager.Request(uri);

        // 设置下载目录，文件名
        String dir = file_save_dir.getPath();
        String name = get_filename();

        Log.i("测试dir ", dir);
        Log.i("测试name ", name);
        request.setDestinationInExternalPublicDir(dir, name);

        // 设置只允许在WIFI的网络下下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
    }


    public void download(UpdateListener listener) {

        // 加入下载队列, 开始下载
        download_id = downloadmanager.enqueue(request);


        // 激活通知栏点击事件
        context.registerReceiver(on_notification_click,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));


        // 下载完成后事件
        context.registerReceiver(on_complete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        this.listener = listener;
        context.getContentResolver().registerContentObserver(
                CONTENT_URI, true, download_observer);
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
    }


    public void set_notification(Class activity_class, Bundle intent_extras) {
        this.activity_class = activity_class;
        this.intent_extras = intent_extras;
    }


    // 通知栏点击逻辑事件处理
    BroadcastReceiver on_notification_click = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Intent i = new Intent(ctxt, activity_class);
            i.putExtras(intent_extras);
            ctxt.startActivity(i);
        }
    };


    // 完成下载后通知栏逻辑
    BroadcastReceiver on_complete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Random rand = new java.util.Random();
            int notice_id = rand.nextInt(999999999);

            final ComponentName receiver = new ComponentName(context, activity_class);
            Intent notice_intent = new Intent(ctxt.getClass().getName() +
                    System.currentTimeMillis());
            notice_intent.setComponent(receiver);


            notice_intent.putExtras(intent_extras);
            notice_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, notice_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification n  = new NotificationCompat.Builder(context)
                    .setContentTitle("hello")
                    .setContentText("world")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true).getNotification();


            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);


            notificationManager.notify(notice_id, n);
        }
    };



    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            try {
                // Thread.sleep(900);
            } catch (Exception e) {

            }

            int downloaded_size = get_downloaded_size();
            Log.i("已经下载的大小 ", Integer.toString(downloaded_size));
            listener.on_update(downloaded_size);
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
                Log.i("下载状态 ", Integer.toString(bytes_and_status[0]));
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

        int[] bytes_and_status = get_bytes_and_status();
        return bytes_and_status[1];

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
        }
    }




}
