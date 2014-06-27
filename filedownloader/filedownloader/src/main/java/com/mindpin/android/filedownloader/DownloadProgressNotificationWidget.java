package com.mindpin.android.filedownloader;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadProgressNotificationWidget extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("hello 我在这 ", "true");

        Intent download_service = new Intent(context, DownloadService.class);
        download_service.putExtra("should_stop_foreground", true);
        context.startService(download_service);
    }
}
