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
    private static final Class[] start_signature = new Class[] {
            int.class, Notification.class};
    private static final Class[] stop_signature = new Class[] {
            boolean.class};

    private NotificationManager nm;
    private Method start_foreground_method;
    private Method stop_foreground_method;

    Object[] start_foreground_args = new Object[2];
    Object[] stop_foreground_args = new Object[1];

    DownloadService download_service;
    Context context;


    public NotificationServiceBar(Context context, DownloadService download_service) {
        this.context = context;
        this.download_service = download_service;

        nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public NotificationServiceBar(Context context) {
        this.context = context;
        nm = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
    }


    public void start_foreground(int id, Notification notification) {
        try {
            start_foreground_method = getClass().getMethod("start_foreground",
                    start_signature);
            stop_foreground_method = getClass().getMethod("stop_foreground",
                    stop_signature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            start_foreground_method = stop_foreground_method = null;
        }

        // If we have the new start_foreground API, then use it.
        if (start_foreground_method != null) {
            start_foreground_args[0] = Integer.valueOf(id);
            start_foreground_args[1] = notification;
            try {
                start_foreground_method.invoke(this, start_foreground_args);
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
        nm.notify(id, notification);
    }

    public void stop_foreground(int id) {
        // If we have the new stop_foreground API, then use it.
        if (stop_foreground_method != null) {
            stop_foreground_args[0] = Boolean.TRUE;
            try {
                stop_foreground_method.invoke(this, stop_foreground_args);
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
        nm.cancel(id);
        // setForeground(false);
    }


    public void handle_notification(FileDownloader file_downloader, int notice_id) {
        String downloaded_size = download_service.show_human_size(file_downloader.downloaded_size);
        String file_size = download_service.show_human_size(file_downloader.file_size);

        float num = (float) file_downloader.downloaded_size/
                (float) file_downloader.file_size;
        int result = (int)(num*100);
        String percentage = Integer.toString(result);


        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Download")
                .setContentText(Integer.toString(file_downloader.downloaded_size))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        Notification notification = mBuilder.getNotification();

        RemoteViews content_view = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        content_view.setImageViewResource(R.id.progress_notify_image, R.drawable.ic_launcher);

        content_view.setTextViewText(R.id.progress_title_text,
                regenerate_filename(file_downloader.get_file_name()));

        content_view.setTextViewText(R.id.download_filename, "");

        content_view.setTextViewText(R.id.progress_percentage, downloaded_size + " / " + file_size);
        Log.i("显示正在下载的大小 ", Integer.toString(file_downloader.downloaded_size));
        Log.i("显示文件的大小 ", Integer.toString(file_downloader.file_size));
        content_view.setProgressBar(R.id.download_progressbar_in_service,
                file_downloader.file_size,
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
        if (file_downloader.intent_extras != null) {
            notice_intent.putExtras(file_downloader.intent_extras);
        }


//        PendingIntent p_intent = PendingIntent.getActivity(file_downloader.context,
//                0, notice_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent p_intent = PendingIntent.getBroadcast(file_downloader.context,
                0, notice_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.contentIntent = p_intent;

        content_view.setOnClickPendingIntent(R.id.progress_content_layout, p_intent);

        notification.contentView = content_view;


        start_foreground(notice_id, notification);

    }


    public void wait_notification(FileDownloader file_downloader, int notice_id) {
        String downloaded_size;
        String file_size;

        Log.i("等待状态", "true");

//        String downloaded_size = Integer.toString(0);
//        String file_size = Integer.toString(0);



        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new android.support.v4.app.NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Download")
                .setContentText("等待下载")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        Notification notification = mBuilder.getNotification();

        RemoteViews content_view = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        content_view.setImageViewResource(R.id.progress_notify_image, R.drawable.ic_launcher);
        content_view.setTextViewText(R.id.progress_title_text,
                regenerate_filename(file_downloader.get_file_name()));

        if (download_service == null) {

            downloaded_size = "0";
            file_size = "0";

        } else {

            downloaded_size = download_service.show_human_size(file_downloader.downloaded_size);
            file_size = download_service.show_human_size(file_downloader.file_size);
        }


        content_view.setTextViewText(R.id.download_filename, "");

        content_view.setTextViewText(R.id.progress_percentage, downloaded_size + " / " + file_size);
        Log.i("显示正在下载的大小 ", Integer.toString(file_downloader.downloaded_size));

        content_view.setProgressBar(R.id.download_progressbar_in_service,
                file_downloader.file_size,
                file_downloader.downloaded_size, false);




        final ComponentName receiver = new ComponentName(file_downloader.context,
                DownloadProgressNotificationWidget.class);
        Intent notice_intent = new Intent(file_downloader.context.getClass().getName() +
                System.currentTimeMillis());
        notice_intent.setComponent(receiver);


        PendingIntent p_intent = PendingIntent.getBroadcast(file_downloader.context,
                0, notice_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.contentIntent = p_intent;

        content_view.setOnClickPendingIntent(R.id.progress_content_layout, p_intent);

        notification.contentView = content_view;


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(notice_id, notification);

    }

    public String regenerate_filename(String filename) {
        int size = filename.length();
        if (size <= 16) {
            return filename;
        }

        String short_filename = filename.substring(0, 8) + "..." +
                filename.substring(size - 5);
        return short_filename;
    }
}
