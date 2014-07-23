package com.mindpin.android.filedownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



public class DownloadStopReceiver extends BroadcastReceiver {
    public FileDownloader fd;
    public int file_size = 0;
    public int downloaded_size = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("调试 stop 接收最新 fd ", "true");

        fd = intent.getParcelableExtra("download_manager");
        Log.i("调试 stop 接收最新 fd file_size ", Integer.toString(fd.file_size));
        file_size = fd.file_size;

        Log.i("调试 stop 接收最新 fd downloaded_size ", Integer.toString(fd.downloaded_size));
        downloaded_size = fd.downloaded_size;

    }
}