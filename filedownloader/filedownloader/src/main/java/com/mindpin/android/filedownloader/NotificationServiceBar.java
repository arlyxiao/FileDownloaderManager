package com.mindpin.android.filedownloader;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NotificationServiceBar {
    private static final Class[] mStartForegroundSignature = new Class[] {
            int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {
            boolean.class};

    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    Notification notification;
    RemoteViews content_view;
    int notice_id;
    DownloadService download_service;
    Context context;


    public NotificationServiceBar(Context context, DownloadService download_service) {
        this.context = context;
        this.download_service = download_service;
        notice_id = download_service.notice_id;
    }


    public void startForeground(int id, Notification notification) {
        notice_id = id;
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.i("无法启动前台服务 ", e.getMessage());
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.i("无法启动前台服务 ", e.getMessage());
            }
            return;
        }

        // Fall back on the old API.
        // setForeground(true);
        mNM.notify(id, notification);
    }

    public void stopForeground(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.i("无法关掉前台服务 ", e.getMessage());
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.i("无法关掉前台服务 ", e.getMessage());
            }
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        // setForeground(false);
    }


    public void handle_notification(FileDownloader file_downloader) {

        String downloaded_size = download_service.show_human_size(file_downloader.downloaded_size);
        String file_size = download_service.show_human_size(file_downloader.file_size);

        float num = (float) file_downloader.downloaded_size/
                (float) file_downloader.file_size;
        int result = (int)(num*100);
        String percentage = Integer.toString(result);

        mNM = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }


        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Download")
                .setContentText(Integer.toString(file_downloader.downloaded_size))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        notification = mBuilder.getNotification();

        content_view = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        content_view.setImageViewResource(R.id.progress_notify_image, R.drawable.ic_launcher);

        content_view.setTextViewText(R.id.progress_title_text,
                download_service.regenerate_filename(file_downloader.get_file_name()));

        content_view.setTextViewText(R.id.download_filename, "");

        content_view.setTextViewText(R.id.progress_percentage, downloaded_size + " / " + file_size);
        Log.i("显示正在下载的大小 ", Integer.toString(file_downloader.downloaded_size));
        content_view.setProgressBar(R.id.download_progressbar_in_service,
                file_downloader.get_file_size(),
                file_downloader.downloaded_size, false);




//        final ComponentName receiver = new ComponentName(file_downloader.context,
//                file_downloader.activity_class);
        final ComponentName receiver = new ComponentName(file_downloader.context,
                DownloadProgressNotificationWidget.class);
        Intent notice_intent = new Intent(file_downloader.context.getClass().getName() +
                System.currentTimeMillis());
        notice_intent.setComponent(receiver);



//        String param_name1 = file_downloader.intent_extras.getString("param_name1");
//        Log.i("测试值 ", param_name1);
        notice_intent.putExtras(file_downloader.intent_extras);

//        PendingIntent p_intent = PendingIntent.getActivity(file_downloader.context,
//                0, notice_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent p_intent = PendingIntent.getBroadcast(file_downloader.context,
                0, notice_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.contentIntent = p_intent;

        content_view.setOnClickPendingIntent(R.id.progress_content_layout, p_intent);

        notification.contentView = content_view;

        startForeground(notice_id, notification);

    }
}
