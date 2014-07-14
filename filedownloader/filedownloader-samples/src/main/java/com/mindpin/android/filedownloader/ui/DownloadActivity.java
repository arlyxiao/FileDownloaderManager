package com.mindpin.android.filedownloader.ui;

import android.app.Activity;
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
    public TextView result_view;
    private TextView downloaded_file_view;
    public ProgressBar progress_bar;
    String downloaded_file;
    String stored_dir;

    String path_less_100kb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E9%80%9A%E7%94%A8LOADING%E6%8F%90%E7%A4%BA%E7%BB%84%E4%BB%B6.png";
    String path_less_1mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%A4%B4%E5%83%8F%E6%88%AA%E5%8F%96.png";
    String path_less_5mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%9B%BE%E7%89%87%E6%94%BE%E5%A4%A7%E7%BC%A9%E5%B0%8F%E6%97%8B%E8%BD%AC.mp4";
    String path_less_10mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/KCExtraImageView.mp4";
    String path_above_10mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/jihuang.mp4";


    FileDownloader fd2;


    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:



                    if(progress_bar.getProgress()== progress_bar.getMax()){
                        Toast.makeText(DownloadActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        downloaded_file_view.setText(stored_dir + "/" + downloaded_file);


//                        try {
//                            Intent intent = new Intent(Intent.ACTION_VIEW);
//                            File file = new File(stored_dir + "/" + downloaded_file);
//                            intent.setAction(android.content.Intent.ACTION_VIEW);
//                            intent.setDataAndType(Uri.fromFile(file),getMimeType(file.getAbsolutePath()));
//                            startActivity(intent);
//                        } catch (Exception e) {
//                            Log.i("文件打开错误 ", e.getMessage());
//                        }


                    }
                    break;

                case -1:
                    Toast.makeText(DownloadActivity.this, R.string.error, 1).show();
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("应该是在 当前UI线程 ", Long.toString(Thread.currentThread().getId()));

        stored_dir = Environment.getExternalStorageDirectory().toString();


        progress_bar = (ProgressBar) this.findViewById(R.id.downloadbar);
        result_view = (TextView) this.findViewById(R.id.result_view);
        downloaded_file_view = (TextView) this.findViewById(R.id.downloaded_file);

        Button button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_100kb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", stored_dir);
                    File savedir = Environment.getExternalStorageDirectory();
                    FileDownloader fd =
                            new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    download(fd);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button2 = (Button) this.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_1mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = Environment.getExternalStorageDirectory();
                    fd2 =
                            new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    download(fd2);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button3 = (Button) this.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_5mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = Environment.getExternalStorageDirectory();
                    FileDownloader fd =
                            new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    download(fd);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button4 = (Button) this.findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_less_10mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = Environment.getExternalStorageDirectory();
                    FileDownloader fd =
                            new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    download(fd);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button5 = (Button) this.findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_above_10mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    File savedir = Environment.getExternalStorageDirectory();
                    FileDownloader fd =
                            new FileDownloader(DownloadActivity.this, path, savedir, 2);
                    download(fd);
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        // 暂停下载
        Button button6 = (Button) this.findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fd2.pause_download();
            }
        });


        // 停止下载并删除文件
        Button button7 = (Button) this.findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void download(final FileDownloader fd) {

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                Bundle b = new Bundle();
                b.putString("param_name1", "param_value1");
                fd.set_notification(TargetActivity.class, b);
                downloaded_file = fd.get_file_name();
                try {
                    Log.i("activity 中取下载URL ", fd.download_url);
                    fd.download(new ProgressUpdateListener () {
                        @Override
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
                                Toast.makeText(DownloadActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                                downloaded_file_view.setText(stored_dir + "/" + downloaded_file);

                            }

//                    Message msg = new Message();
//                    msg.what = 1;
//                    msg.getData().putInt("size", downloaded_size);
//                    handler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    handler.obtainMessage(-1).sendToTarget();
                    Log.i("下载错误 ", e.getMessage());
                    Log.i("下载错误 ", e.toString());
                    e.printStackTrace();
                }
//            }
//        }).start();


    }
}