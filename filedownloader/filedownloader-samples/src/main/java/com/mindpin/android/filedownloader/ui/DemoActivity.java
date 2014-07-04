package com.mindpin.android.filedownloader.ui;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mindpin.android.filedownloader.R;

import java.io.File;
import java.text.DecimalFormat;


public class DemoActivity extends Activity {
    Context context;
    private Button less_5mb_btn, less_10mb_btn, remove_btn;
    private ProgressBar progress_bar;
    private TextView percentage_view, present_view;

    int filesize;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down);

        context = getApplicationContext();

        progress_bar = (ProgressBar) this.findViewById(R.id.downloadbar);
        present_view = (TextView) this.findViewById(R.id.present_view);
        percentage_view = (TextView) this.findViewById(R.id.percentage_view);

        progress_bar.setMax(0);
        progress_bar.setProgress(0);

        less_5mb_btn = (Button) findViewById(R.id.less_5mb_btn);
        less_5mb_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String download_url = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%9B%BE%E7%89%87%E6%94%BE%E5%A4%A7%E7%BC%A9%E5%B0%8F%E6%97%8B%E8%BD%AC.mp4";
                File save_file_path = Environment.getExternalStorageDirectory();
                final DownloadLib fd = new DownloadLib(context, download_url, save_file_path);

                Bundle b = new Bundle();
                b.putString("param_name1", "param_value1");
                fd.set_notification(TargetActivity.class, b);
                filesize = fd.get_filesize();
                Log.i("文件总大小 ", Integer.toString(filesize));
                fd.download(new UpdateListener(){
                    public void on_update(int downloaded_size){
                        Log.i("UI界面已经下载的大小 ", Integer.toString(downloaded_size));

//                        int filesize = fd.get_filesize();

                        progress_bar.setMax(filesize);
                        progress_bar.setProgress(downloaded_size);

                        percentage_view.setText(get_percent(downloaded_size, filesize));
                        present_view.setText(get_size(downloaded_size) + "/" + get_size(filesize));
                    }
                });


            }
        });




        less_10mb_btn = (Button) findViewById(R.id.less_10mb_btn);
        less_10mb_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String download_url = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/KCExtraImageView.mp4";
                File save_file_path = Environment.getExternalStorageDirectory();
                final DownloadLib fd = new DownloadLib(context, download_url, save_file_path);

                Bundle b = new Bundle();
                b.putString("param_name1", "param_value1");
                fd.set_notification(TargetActivity.class, b);
                filesize = fd.get_filesize();
                Log.i("文件总大小 ", Integer.toString(filesize));
                fd.download(new UpdateListener(){
                    public void on_update(int downloaded_size){
                        Log.i("UI界面已经下载的大小 ", Integer.toString(downloaded_size));

//                        int filesize = fd.get_filesize();

                        progress_bar.setMax(filesize);
                        progress_bar.setProgress(downloaded_size);

                        percentage_view.setText(get_percent(downloaded_size, filesize));
                        present_view.setText(get_size(downloaded_size) + "/" + get_size(filesize));
                    }
                });

            }
        });
    }





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