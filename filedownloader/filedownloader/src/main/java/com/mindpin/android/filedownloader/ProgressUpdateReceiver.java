package com.mindpin.android.filedownloader;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;


public class ProgressUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i("收到通知 ", "true");

        Bundle bundle = intent.getExtras();

        int downloaded_size = bundle.getInt("downloaded_size");

        NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Download")
                .setContentText(Integer.toString(downloaded_size))
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis());

        Notification notification = mBuilder.getNotification();

//        int icon = R.drawable.ic_launcher;
//        long when = System.currentTimeMillis();
//        Notification notification = new Notification(icon, "Custom Notification", when);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        contentView.setImageViewResource(R.id.progress_notify_image, R.drawable.ic_launcher);
        contentView.setTextViewText(R.id.progress_title_text, Integer.toString(downloaded_size));
        notification.contentView = contentView;

        Intent notificationIntent = new Intent("");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;


        mNotifyManager.notify(2, notification);
    }
}