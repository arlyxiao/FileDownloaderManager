package com.mindpin.android.filedownloader;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class DownloadService extends Service {
    FileDownloader file_downloader;

    @Override
    public void onCreate() {
        Log.i("开始运行 download service ", "true");

    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i("服务开始启动了 ", "true");

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
    }


    public int download(FileDownloader file_downloader, ProgressUpdateListener listener)
            throws Exception{

        this.file_downloader = file_downloader;
        init_connection(file_downloader,
                file_downloader.context,
                file_downloader.download_url,
                file_downloader.save_file,
                file_downloader.thread_num);



        try {
            RandomAccessFile rand_out = new RandomAccessFile(this.file_downloader.save_file, "rw");
            if(this.file_downloader.file_size >0) rand_out.setLength(this.file_downloader.file_size);
            rand_out.close();
            URL url = new URL(this.file_downloader.download_url);
            if(this.file_downloader.thread_data.size() != this.file_downloader.threads.length){
                this.file_downloader.thread_data.clear();
                for (int i = 0; i < this.file_downloader.threads.length; i++) {
                    this.file_downloader.thread_data.put(i+1, 0);
                }
            }
            for (int i = 0; i < this.file_downloader.threads.length; i++) {
                int downLength = this.file_downloader.thread_data.get(i+1);
                if(downLength < this.file_downloader.block && this.file_downloader.downloaded_size <this.file_downloader.file_size){
                    this.file_downloader.threads[i] = new DownloadThread(
                            this.file_downloader,
                            url,
                            this.file_downloader.save_file,
                            this.file_downloader.block,
                            this.file_downloader.thread_data.get(i+1),
                            i+1);

                    this.file_downloader.threads[i].setPriority(7);
                    this.file_downloader.threads[i].start();
                }else{
                    this.file_downloader.threads[i] = null;
                }
            }
            this.file_downloader.file_record.save(this.file_downloader.download_url,
                    this.file_downloader.thread_data);
            boolean not_finish = true;
            while (not_finish) {
                Thread.sleep(900);
                not_finish = false;
                for (int i = 0; i < this.file_downloader.threads.length; i++){
                    if (this.file_downloader.threads[i] != null && !this.file_downloader.threads[i].is_finish()) {
                        not_finish = true;
                        if(this.file_downloader.threads[i].get_downloaded_length() == -1){
                            this.file_downloader.threads[i] = new DownloadThread(this.file_downloader, url,
                                    this.file_downloader.save_file,
                                    this.file_downloader.block,
                                    this.file_downloader.thread_data.get(i+1), i+1);
                            this.file_downloader.threads[i].setPriority(7);
                            this.file_downloader.threads[i].start();
                        }
                    }
                }

//                Intent in = new Intent("app.action.update_progress");
//                in.putExtra("downloaded_size", file_downloader.downloaded_size);
//                getApplicationContext().sendBroadcast(in);

                handleCommand(file_downloader);

                if(listener!=null) listener.on_update(this.file_downloader.downloaded_size);
            }
            this.file_downloader.file_record.delete(this.file_downloader.download_url);
        } catch (Exception e) {
            Log.i("文件下载错误 ", e.getMessage());
            throw new Exception("file download fail");
        }






        return this.file_downloader.downloaded_size;
    }


    private void init_connection(FileDownloader file_downloader,
                                 Context context,
                                 String download_url,
                                 File file_save_dir,
                                 int thread_num) {
        try {
            Log.i("下载的 URL ", download_url);
            file_downloader.context = context;
            file_downloader.download_url = download_url;
            file_downloader.file_record = new FileRecord(file_downloader.context);
            URL url = new URL(file_downloader.download_url);
            if(!file_save_dir.exists()) file_save_dir.mkdirs();
            file_downloader.threads = new DownloadThread[thread_num];
            file_downloader.conn = (HttpURLConnection) url.openConnection();
            file_downloader.conn.setConnectTimeout(5*1000);
            file_downloader.conn.setRequestMethod("GET");
            file_downloader.conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            file_downloader.conn.setRequestProperty("Accept-Language", "zh-CN");
            file_downloader.conn.setRequestProperty("Referer", download_url);
            file_downloader.conn.setRequestProperty("Charset", "UTF-8");
            file_downloader.conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            file_downloader.conn.setRequestProperty("Connection", "Keep-Alive");
            file_downloader.conn.connect();
            file_downloader.print_response_header(file_downloader.conn);
            if (file_downloader.conn.getResponseCode()==200) {
                file_downloader.file_size = file_downloader.conn.getContentLength();
                Log.i("初始化连接 文件大小 ", Integer.toString(file_downloader.file_size));
                if (file_downloader.file_size <= 0) throw new RuntimeException("Unkown file size ");

                String filename = file_downloader.get_file_name();
                file_downloader.save_file = new File(file_save_dir, filename);
                Map<Integer, Integer> logdata = file_downloader.file_record.get_data(download_url);
                if(logdata.size()>0){
                    for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
                        file_downloader.thread_data.put(entry.getKey(), entry.getValue());
                }
                if(file_downloader.thread_data.size()==file_downloader.threads.length){
                    for (int i = 0; i < file_downloader.threads.length; i++) {
                        file_downloader.downloaded_size += file_downloader.thread_data.get(i+1);
                    }
                    file_downloader.print("已经下载的长度 "+ file_downloader.downloaded_size);
                }
                //计算每条线程下载的数据长度
                file_downloader.block = (file_downloader.file_size % file_downloader.threads.length)==0?
                        file_downloader.file_size / file_downloader.threads.length :
                        file_downloader.file_size / file_downloader.threads.length + 1;
            }else{
                throw new RuntimeException("server no response ");
            }
        } catch (Exception e) {
            file_downloader.print(e.toString());
            Log.i("下载错误 ", e.getMessage());
            throw new RuntimeException("don't connection this url");
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        return m_binder;
    }

    private final IBinder m_binder = new LocalBinder();






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
    RemoteViews contentView;


    public void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }

        // Fall back on the old API.
        // setForeground(true);
        mNM.notify(id, notification);
    }


    void handleCommand(FileDownloader file_downloader) {

        String downloaded_size = show_human_size(file_downloader.downloaded_size);
        String file_size = show_human_size(file_downloader.file_size);

        float num = (float) file_downloader.downloaded_size/
                (float) file_downloader.file_size;
        int result = (int)(num*100);
        String percentage = Integer.toString(result);

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setContentTitle("Download")
                .setContentText(Integer.toString(file_downloader.downloaded_size))
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis());
        notification = mBuilder.getNotification();

        contentView = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);
        contentView.setImageViewResource(R.id.progress_notify_image, R.drawable.ic_launcher);
        contentView.setTextViewText(R.id.progress_title_text,
                file_downloader.get_file_name().substring(0,7) + " " + percentage + "%");
        contentView.setTextViewText(R.id.progress_percentage, downloaded_size + " / " + file_size);
        Log.i("显示正在下载的大小 ", Integer.toString(file_downloader.downloaded_size));
        contentView.setProgressBar(R.id.download_progressbar_in_service,
                file_downloader.get_file_size(),
                file_downloader.downloaded_size, false);

        notification.contentView = contentView;

        Intent notificationIntent = new Intent("");
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;


        startForegroundCompat(R.string.foreground_service_started, notification);
    }

    public String show_human_size(long bytes) {
        Boolean si = true;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}