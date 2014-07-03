package com.mindpin.android.filedownloader.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.mindpin.android.filedownloader.R;

import java.text.DecimalFormat;


@SuppressLint("NewApi")
public class SystemDownloadActivity extends Activity {
    private Button less_5mb_btn, less_10mb_btn, remove_btn;
    private ProgressBar progress_bar;
    private TextView percentage_view, present_view;
    DownloadManager downloadmanager;
    DownloadChangeObserver download_observer;
    Long download_id;
    private MyHandler handler;
    Uri uri;

    public static final Uri CONTENT_URI   = Uri.parse("content://downloads/my_downloads");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down);


        registerReceiver(on_complete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        registerReceiver(on_notification_click,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

        handler = new MyHandler();
        String servicestring = Context.DOWNLOAD_SERVICE;
        downloadmanager = (DownloadManager) getSystemService(servicestring);
        download_observer = new DownloadChangeObserver();

        progress_bar = (ProgressBar) this.findViewById(R.id.downloadbar);
        present_view = (TextView) this.findViewById(R.id.present_view);
        percentage_view = (TextView) this.findViewById(R.id.percentage_view);


        less_5mb_btn = (Button) findViewById(R.id.less_5mb_btn);
        less_5mb_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                uri = Uri
                        .parse("http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%9B%BE%E7%89%87%E6%94%BE%E5%A4%A7%E7%BC%A9%E5%B0%8F%E6%97%8B%E8%BD%AC.mp4");
                DownloadManager.Request request = new Request(uri);

                // 设置下载目录，文件名
                request.setDestinationInExternalPublicDir("mindpin", "less_5mb.mp4");

                // 设置只允许在WIFI的网络下下载
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

                download_id = downloadmanager.enqueue(request);

                getContentResolver().registerContentObserver(CONTENT_URI, true, download_observer);

                update_progress();

            }
        });


        less_10mb_btn = (Button) findViewById(R.id.less_10mb_btn);
        less_10mb_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                uri = Uri
                        .parse("http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/KCExtraImageView.mp4");
                DownloadManager.Request request = new Request(uri);

                // 设置下载目录，文件名
                request.setDestinationInExternalPublicDir("mindpin", "less_10mb.mp4");

                // 设置只允许在WIFI的网络下下载
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

                download_id = downloadmanager.enqueue(request);

                getContentResolver().registerContentObserver(CONTENT_URI, true, download_observer);

            }
        });

        remove_btn = (Button) findViewById(R.id.remove_btn);
        remove_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadmanager.remove(download_id);
                // downloadManagerPro.pauseDownload(download_id);
                // getContentResolver().unregisterContentObserver(download_observer);
                Log.i("删除下载", "true");
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, download_observer);
//        // update_progress();
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        getContentResolver().unregisterContentObserver(download_observer);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(on_complete);
        unregisterReceiver(on_notification_click);
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            update_progress();
        }

    }

    public void update_progress() {
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
        } finally {
            if (c != null) {
                c.close();
            }
        }

        handler.sendMessage(handler.obtainMessage(0, bytes_and_status[0], bytes_and_status[1], bytes_and_status[2]));
    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Context context = getApplicationContext();

            switch (msg.what) {
                case 0:
                    int status = (Integer)msg.obj;
                    if (is_downloading(status)) {
                        progress_bar.setVisibility(View.VISIBLE);
                        progress_bar.setMax(0);
                        progress_bar.setProgress(0);

                        if (msg.arg2 < 0) {
                            Log.i("参数 ", Integer.toString(msg.arg2));
                            progress_bar.setIndeterminate(true);
                            percentage_view.setText("0%");
                            present_view.setText("0M/0M");
                        } else {
                            Log.i("参数 ", Integer.toString(msg.arg2));
                            progress_bar.setIndeterminate(false);
                            progress_bar.setMax(msg.arg2);
                            progress_bar.setProgress(msg.arg1);
                            percentage_view.setText(get_percent(msg.arg1, msg.arg2));
                            present_view.setText(get_size(msg.arg1) + "/" + get_size(msg.arg2));
                        }
                    } else {
                        progress_bar.setVisibility(View.GONE);
                        progress_bar.setMax(0);
                        progress_bar.setProgress(0);

                        if (status == DownloadManager.STATUS_FAILED) {
                            Toast.makeText(context, "下载失败", Toast.LENGTH_LONG).show();
                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(context, "下载成功", Toast.LENGTH_LONG).show();
                        } else {

                        }
                    }
                    break;
            }
        }
    }


    public static boolean is_downloading(int status) {
        return status == DownloadManager.STATUS_RUNNING
                || status == DownloadManager.STATUS_PAUSED
                || status == DownloadManager.STATUS_PENDING;
    }



    BroadcastReceiver on_notification_click = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "通知栏点击提示", Toast.LENGTH_LONG).show();
        }
    };


    BroadcastReceiver on_complete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "已经下载完成提示", Toast.LENGTH_LONG).show();
        }
    };








    public static final int    MB_2_BYTE             = 1024 * 1024;
    public static final int    KB_2_BYTE             = 1024;
    static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");


    public static CharSequence get_size(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / MB_2_BYTE)).append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / KB_2_BYTE)).append("K");
        } else {
            return size + "B";
        }
    }

    public static String get_percent(long progress, long max) {
        int rate = 0;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int)((double)progress / max * 100);
        }
        return new StringBuilder(16).append(rate).append("%").toString();
    }



}