package com.mindpin.android.filedownloader;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class StartServiceThread extends Thread {
    public FileDownloader download_manager;
    public ProgressUpdateListener listener;
    public Context context;

    public StartServiceThread(FileDownloader download_manager,
                              ProgressUpdateListener listener) {
        this.context = download_manager.context;
        this.download_manager = download_manager;
        this.listener = listener;

    }

    @Override
    public void run() {
        Intent download_service = new Intent(context, DownloadService.class);
        download_service.putExtra("download_manager", download_manager);
        context.startService(download_service);

        final BroadcastReceiver progress_listener_receiver = new DownloadListenerReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {

                fd = intent.getParcelableExtra("download_manager");
                Log.i("接收正在下载的 downloaded_size 值 ", Integer.toString(fd.downloaded_size));

                download_manager.file_size = fd.file_size;

                Log.i("接收正在下载的 file_size 值 ", Integer.toString(fd.file_size));

                download_manager.listener = listener;
                download_manager.listener.on_update(fd.downloaded_size);
            }
        };

        context.registerReceiver(progress_listener_receiver,
                new IntentFilter("app.action.download_listener_receiver"));
    }
}