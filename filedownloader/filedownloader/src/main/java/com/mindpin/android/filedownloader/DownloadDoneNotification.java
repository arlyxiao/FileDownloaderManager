package com.mindpin.android.filedownloader;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Random;


public class DownloadDoneNotification extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("已经下载完成 停止服务后 显示的通知 ", "true");

//        Intent download_service = new Intent(context, DownloadService.class);
//        download_service.putExtra("should_stop_foreground", true);
//        context.startService(download_service);

        Bundle extras = intent.getExtras();
        String target_activity_name = intent.getStringExtra("activity_class");
        Class<?> target_activity = null;

        try {
            target_activity = Class.forName(target_activity_name);
        } catch (Exception e) {
            Log.i(" String 转换成 Class 错误 ", e.getMessage());
        }


        Log.i("目标 activity ", target_activity.getName());
        String filename = intent.getStringExtra("filename");
        String file_size = intent.getStringExtra("file_size");


        final ComponentName receiver = new ComponentName(context, target_activity);
        Intent notice_intent = new Intent(context.getClass().getName() +
                System.currentTimeMillis());
        notice_intent.setComponent(receiver);

        notice_intent.putExtras(extras);
        notice_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent p_intent = PendingIntent.getActivity(context, 0, notice_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n  = new NotificationCompat.Builder(context)
                .setContentTitle(filename)
                .setContentText("下载完成 " + file_size)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(p_intent)
                .setAutoCancel(true).getNotification();


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);


        Random rand = new Random();
        int notice_id = rand.nextInt(999999999);
        notificationManager.notify(notice_id, n);
    }
}