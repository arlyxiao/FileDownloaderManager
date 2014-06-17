package com.mindpin.android.filedownloader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mindpin.android.filedownloader.FileDownloader;
import com.mindpin.android.filedownloader.ProgressUpdateListener;
import com.mindpin.android.filedownloader.R;

import java.io.File;




public class DownloadActivity extends Activity {
    private TextView resultView;
    private ProgressBar progressBar;

    String path_small_100kb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E9%80%9A%E7%94%A8LOADING%E6%8F%90%E7%A4%BA%E7%BB%84%E4%BB%B6.png";
    String path_small_1mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%A4%B4%E5%83%8F%E6%88%AA%E5%8F%96.png";
    String path_small_5mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%9B%BE%E7%89%87%E6%94%BE%E5%A4%A7%E7%BC%A9%E5%B0%8F%E6%97%8B%E8%BD%AC.mp4";
    String path_small_10mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/KCExtraImageView.mp4";
    String path_above_10mb = "http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/jihuang.mp4";


    private Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    progressBar.setProgress(msg.getData().getInt("size"));
                    Log.i("正在进行的进度条大小0 ", Integer.toString(msg.getData().getInt("size")));
                    Log.i("正在进行的进度条大小1 ", Integer.toString(progressBar.getProgress()));
                    Log.i("进度条最大大小 ", Integer.toString(progressBar.getMax()));

                    float num = (float)progressBar.getProgress()/(float)progressBar.getMax();
                    int result = (int)(num*100);
                    resultView.setText(result+ "%");
                    if(progressBar.getProgress()==progressBar.getMax()){
                        Toast.makeText(DownloadActivity.this, R.string.success, 0).show();
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


        progressBar = (ProgressBar) this.findViewById(R.id.downloadbar);
        resultView = (TextView) this.findViewById(R.id.result);
        Button button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_small_100kb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    download(path, Environment.getExternalStorageDirectory());
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button2 = (Button) this.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_small_1mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    download(path, Environment.getExternalStorageDirectory());
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button3 = (Button) this.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_small_5mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    download(path, Environment.getExternalStorageDirectory());
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });


        Button button4 = (Button) this.findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = path_small_10mb;
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    Log.i("存储的路径 ", Environment.getExternalStorageDirectory().toString());
                    download(path, Environment.getExternalStorageDirectory());
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
                    download(path, Environment.getExternalStorageDirectory());
                }else{
                    Toast.makeText(DownloadActivity.this, R.string.sdcarderror, 1).show();
                }

            }
        });
    }
    //主线程(UI线程)
    //业务逻辑正确，但是该程序运行的时候有问题
    //对于显示控件的界面更新只是由UI线程负责，如果是在非UI线程更新控件的属性值，更新后的显示界面不会反映到屏幕上
    private void download(final String path, final File savedir) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileDownloader loader = new FileDownloader(DownloadActivity.this, path, savedir, 8);
                progressBar.setMax(loader.get_file_size());
                try {
                    loader.download(new ProgressUpdateListener() {
                        @Override
                        public void on_update(int downloaded_size) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.getData().putInt("size", downloaded_size);
                            Log.i("已经下载了多大 ", Integer.toString(downloaded_size));
                            handler.sendMessage(msg);//发送消息
                        }
                    });
                } catch (Exception e) {
                    handler.obtainMessage(-1).sendToTarget();
                    Log.i("下载错误 ", e.getMessage());
                }
            }
        }).start();
    }
}