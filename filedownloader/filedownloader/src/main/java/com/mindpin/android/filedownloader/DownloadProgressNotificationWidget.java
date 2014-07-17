package com.mindpin.android.filedownloader;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class DownloadProgressNotificationWidget extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("hello 我在这 ", "true");

        Bundle extras = intent.getExtras();
        String target_activity_name = intent.getStringExtra("activity_class");
        Class<?> target_activity = null;

        try {
            target_activity = Class.forName(target_activity_name);
        } catch (Exception e) {
            Log.i(" String 转换成 Class 错误 ", e.getMessage());
        }


        Log.i("目标 activity ", target_activity.getName());


        Intent download_service = new Intent(context, DownloadService.class);
        download_service.putExtra("should_stop_foreground", true);
        context.startService(download_service);
    }
}
