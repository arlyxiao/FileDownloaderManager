package com.mindpin.android.filedownloader.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mindpin.android.filedownloader.FileDownloader;
import com.mindpin.android.filedownloader.ProgressUpdateListener;
import com.mindpin.android.filedownloader.R;

import java.io.File;
import java.util.Scanner;


public class DownloadActivity extends Activity {
    public TextView result_view, result_view1,
    result_view2, result_view3, result_view4, result_view5;

    private TextView downloaded_file_view;
    public ProgressBar progress_bar, progress_bar1,
            progress_bar2, progress_bar3, progress_bar4, progress_bar5;
    String downloaded_file;
    String stored_dir;

    String path_less_100kb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E9%80%9A%E7%94%A8LOADING%E6%8F%90%E7%A4%BA%E7%BB%84%E4%BB%B6.png";
    String path_less_1mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%A4%B4%E5%83%8F%E6%88%AA%E5%8F%96.png";
    String path_less_5mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%9B%BE%E7%89%87%E6%94%BE%E5%A4%A7%E7%BC%A9%E5%B0%8F%E6%97%8B%E8%BD%AC.mp4";
    String path_less_10mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/KCExtraImageView.mp4";
    String path_above_10mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/jihuang.mp4";


    FileDownloader fd_less_100kb;
    FileDownloader fd_less_1m;
    FileDownloader fd_less_5m;
    FileDownloader fd_less_10m;
    FileDownloader fd_more_10m;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("应该是在 当前UI线程 ", Long.toString(Thread.currentThread().getId()));

        // stored_dir = Environment.getExternalStorageDirectory().toString();
        stored_dir = "/testmindpin/files";


        // progress_bar = (ProgressBar) this.findViewById(R.id.downloadbar);

        progress_bar1 = (ProgressBar) this.findViewById(R.id.downloadbar1);
        progress_bar2 = (ProgressBar) this.findViewById(R.id.downloadbar2);
        progress_bar3 = (ProgressBar) this.findViewById(R.id.downloadbar3);
        progress_bar4 = (ProgressBar) this.findViewById(R.id.downloadbar4);
        progress_bar5 = (ProgressBar) this.findViewById(R.id.downloadbar5);

        // result_view = (TextView) this.findViewById(R.id.result_view);

        result_view1 = (TextView) this.findViewById(R.id.result_view1);
        result_view2 = (TextView) this.findViewById(R.id.result_view2);
        result_view3 = (TextView) this.findViewById(R.id.result_view3);
        result_view4 = (TextView) this.findViewById(R.id.result_view4);
        result_view5 = (TextView) this.findViewById(R.id.result_view5);

        downloaded_file_view = (TextView) this.findViewById(R.id.downloaded_file);

        // 100kb 以下
        Button less_100kb_btn = (Button) this.findViewById(R.id.less_100kb_btn);
        less_100kb_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_100kb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", stored_dir);
                    File savedir = new File(stored_dir);
                    if (fd_less_100kb == null) {
                        Log.i("初始化 fd_less_100kb ", "true");
                        fd_less_100kb =
                                new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    }
                    // run_download(fd_less_100kb, result_view1, progress_bar1);
                    run_download_1();
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button less_100kb_btn_pause = (Button) this.findViewById(R.id.less_100kb_btn_pause);
        less_100kb_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_100kb.pause_download();
            }
        });


        Button less_100kb_btn_stop = (Button) this.findViewById(R.id.less_100kb_btn_stop);
        less_100kb_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_100kb.stop_download();
                progress_bar1.setProgress(0);
                result_view1.setText("0%");
            }
        });


        // 1m 以下
        Button less_1m_btn = (Button) this.findViewById(R.id.less_1m_btn);
        less_1m_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_1mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    // File savedir = Environment.getExternalStorageDirectory();
                    File savedir = new File(stored_dir);

                    if (fd_less_1m == null) {
                        Log.i("初始化 fd_less_1m ", "true");
                        fd_less_1m =
                                new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    }
                    // run_download(fd_less_1m, result_view2, progress_bar2);
                    run_download_2();
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });

        Button less_1m_btn_pause = (Button) this.findViewById(R.id.less_1m_btn_pause);
        less_1m_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_1m.pause_download();
            }
        });


        Button less_1m_btn_stop = (Button) this.findViewById(R.id.less_1m_btn_stop);
        less_1m_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_1m.stop_download();
                progress_bar2.setProgress(0);
                result_view2.setText("0%");
            }
        });



        // 5M 以下
        Button less_5m_btn = (Button) this.findViewById(R.id.less_5m_btn);
        less_5m_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_5mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = new File(stored_dir);

                    if (fd_less_5m == null) {
                        Log.i("初始化 fd_less_5m ", "true");
                        fd_less_5m =
                                new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    }
                    run_download(fd_less_5m, result_view3, progress_bar3);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });

        Button less_5m_btn_pause = (Button) this.findViewById(R.id.less_5m_btn_pause);
        less_5m_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_5m.pause_download();
            }
        });


        Button less_5m_btn_stop = (Button) this.findViewById(R.id.less_5m_btn_stop);
        less_5m_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_5m.stop_download();
                progress_bar3.setProgress(0);
                result_view3.setText("0%");
            }
        });


        // 10m以下
        Button less_10m_btn = (Button) this.findViewById(R.id.less_10m_btn);
        less_10m_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_10mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = new File(stored_dir);

                    if (fd_less_10m == null) {
                        Log.i("初始化 fd_less_10m ", "true");
                        fd_less_10m =
                                new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    }
                    run_download(fd_less_10m, result_view4, progress_bar4);

                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button less_10m_btn_pause = (Button) this.findViewById(R.id.less_10m_btn_pause);
        less_10m_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fd_less_10m == null) {
                    Log.i("调试 fd_less_10m 为 null ", "true");
                }
                fd_less_10m.pause_download();

            }
        });


        Button less_10m_btn_stop = (Button) this.findViewById(R.id.less_10m_btn_stop);
        less_10m_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_less_10m.stop_download();
                progress_bar4.setProgress(0);
                result_view4.setText("0%");
            }
        });


        // 10m 以上
        Button more_10m_btn = (Button) this.findViewById(R.id.more_10m_btn);
        more_10m_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_above_10mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = new File(stored_dir);

                    if (fd_more_10m == null) {
                        Log.i("初始化 fd_more_10m ", "true");
                        fd_more_10m =
                                new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    }
                    run_download(fd_more_10m, result_view5, progress_bar5);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });

        Button more_10m_btn_pause = (Button) this.findViewById(R.id.more_10m_btn_pause);
        more_10m_btn_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_more_10m.pause_download();

            }
        });


        Button more_10m_btn_stop = (Button) this.findViewById(R.id.more_10m_btn_stop);
        more_10m_btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd_more_10m.stop_download();
                progress_bar5.setProgress(0);
                result_view5.setText("0%");
            }
        });



    }



    public void run_download(final FileDownloader fd,
                             final TextView result_view,
                             final ProgressBar progress_bar){

        Bundle b = new Bundle();
        b.putString("param_name1", "param_value1");
        fd.set_notification(TargetActivity.class, b);
        downloaded_file = fd.get_file_name();
        try {
            fd.download(new ProgressUpdateListener(){
                public void on_update(int downloaded_size) {
                    Log.i("应该是在 当前UI线程 ", Long.toString(Thread.currentThread().getId()));
                    Log.i("已经下载了多大 ", Integer.toString(downloaded_size));
                    Log.i("文件总大小 ", Integer.toString(fd.get_file_size()));

                    progress_bar.setMax(fd.get_file_size());
                    progress_bar.setProgress(downloaded_size);

                    Log.i("正在进行的进度条大小 ", Integer.toString(progress_bar.getProgress()));
                    Log.i("进度条最大大小 ", Integer.toString(progress_bar.getMax()));

                    float num = (float) progress_bar.getProgress()/(float) progress_bar.getMax();
                    int result = (int)(num*100);
                    Log.i("百分比 ", Integer.toString(result));
                    result_view.setText(Integer.toString(result) + "%");

                    if(progress_bar.getProgress()== progress_bar.getMax()){
                        // Toast.makeText(DownloadActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        downloaded_file_view.setText(stored_dir + "/" + downloaded_file);

                    }
                }
            });
        } catch (Exception e) {
            Log.i("下载错误 ", e.toString());
            e.printStackTrace();
        }

    }




    public void run_download_1(){

        Bundle b = new Bundle();
        b.putString("param_name1", "param_value1");
        fd_less_100kb.set_notification(TargetActivity.class, b);
        downloaded_file = fd_less_100kb.get_file_name();
        try {
            fd_less_100kb.download(new ProgressUpdateListener(){
                public void on_update(int downloaded_size) {
                    Log.i("正在运行 run_download_1 ", "true");
                    Log.i("应该是在 当前UI线程 ", Long.toString(Thread.currentThread().getId()));
                    Log.i("已经下载了多大 ", Integer.toString(downloaded_size));
                    Log.i("文件总大小 ", Integer.toString(fd_less_100kb.get_file_size()));

                    progress_bar1.setMax(fd_less_100kb.get_file_size());
                    progress_bar1.setProgress(downloaded_size);

                    Log.i("正在进行的进度条大小 ", Integer.toString(progress_bar1.getProgress()));
                    Log.i("进度条最大大小 ", Integer.toString(progress_bar1.getMax()));

                    float num = (float) progress_bar1.getProgress()/(float) progress_bar1.getMax();
                    int result = (int)(num*100);
                    Log.i("百分比 ", Integer.toString(result));
                    result_view1.setText(Integer.toString(result) + "%");

                    if(progress_bar1.getProgress()== progress_bar1.getMax()){
                        // Toast.makeText(DownloadActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        downloaded_file_view.setText(stored_dir + "/" + downloaded_file);

                    }
                }
            });
        } catch (Exception e) {
            Log.i("下载错误 ", e.toString());
            e.printStackTrace();
        }

    }


    public void run_download_2(){

        Bundle b = new Bundle();
        b.putString("param_name1", "param_value1");
        fd_less_1m.set_notification(TargetActivity.class, b);
        downloaded_file = fd_less_1m.get_file_name();
        try {
            fd_less_1m.download(new ProgressUpdateListener(){
                public void on_update(int downloaded_size) {
                    Log.i("正在运行 run_download_2 ", "true");
                    Log.i("应该是在 当前UI线程 ", Long.toString(Thread.currentThread().getId()));
                    Log.i("已经下载了多大 ", Integer.toString(downloaded_size));
                    Log.i("文件总大小 ", Integer.toString(fd_less_1m.get_file_size()));

                    progress_bar2.setMax(fd_less_1m.get_file_size());
                    progress_bar2.setProgress(downloaded_size);

                    Log.i("正在进行的进度条大小 ", Integer.toString(progress_bar2.getProgress()));
                    Log.i("进度条最大大小 ", Integer.toString(progress_bar2.getMax()));

                    float num = (float) progress_bar2.getProgress()/(float) progress_bar2.getMax();
                    int result = (int)(num*100);
                    Log.i("百分比 ", Integer.toString(result));
                    result_view2.setText(Integer.toString(result) + "%");

                    if(progress_bar2.getProgress()== progress_bar2.getMax()){
                        // Toast.makeText(DownloadActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        downloaded_file_view.setText(stored_dir + "/" + downloaded_file);

                    }
                }
            });
        } catch (Exception e) {
            Log.i("下载错误 ", e.toString());
            e.printStackTrace();
        }

    }
}