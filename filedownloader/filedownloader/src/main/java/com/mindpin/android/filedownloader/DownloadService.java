package com.mindpin.android.filedownloader;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
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
import java.util.Random;


public class DownloadService extends Service {
    FileDownloader file_downloader;
    boolean not_finish = true;
    int notice_id;
    Boolean should_stop_foreground = false;
    NotificationServiceBar notification_service_bar;
    URL url;

    @Override
    public void onCreate() {
        Log.i("开始运行 download service ", "true");

        Random rand = new Random();
        notice_id = rand.nextInt(999999999);
        notification_service_bar =
                new NotificationServiceBar(getApplicationContext(), this);
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i("服务开始启动了 ", "true");


        should_stop_foreground = intent.getBooleanExtra("should_stop_foreground", false);
        if (should_stop_foreground) {
            Log.i("需要把服务放到后台运行 ", "true");

            Intent i = new Intent(file_downloader.context, file_downloader.activity_class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtras(file_downloader.intent_extras);
            startActivity(i);


            notification_service_bar.stopForeground(notice_id);

            NotificationManager notice_manager =
                    (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notice_manager.cancelAll();


        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i("下载服务关闭 ", "true");
    }


    public int download(final FileDownloader file_downloader, final ProgressUpdateListener listener)
            throws Exception{

        this.file_downloader = file_downloader;

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... objects) {
                try {
                    init_connection(file_downloader.context,
                            file_downloader.download_url,
                            file_downloader.save_file,
                            file_downloader.thread_num);



                    try {
                        save_thread_data();

                        not_finish = true;
                        while (not_finish) {
                            Thread.sleep(900);
                            not_finish = false;
                            for (int i = 0; i < file_downloader.threads.length; i++){
                                if (file_downloader.threads[i] != null && !file_downloader.threads[i].is_finish()) {
                                    not_finish = true;
                                    if(file_downloader.threads[i].get_downloaded_length() == -1){
                                        file_downloader.threads[i] = new DownloadThread(
                                                file_downloader,
                                                url,
                                                file_downloader.save_file,
                                                file_downloader.block,
                                                file_downloader.thread_data.get(i+1), i+1);
                                        file_downloader.threads[i].setPriority(7);
                                        file_downloader.threads[i].start();
                                    }
                                }
                            }


                            notification_service_bar.handle_command(DownloadService.this.file_downloader);

                            // if(listener!=null) listener.on_update(file_downloader.downloaded_size);
                            if (listener != null) {
                                publishProgress();
                            }
                        }
                        file_downloader.file_record.delete(file_downloader.download_url);


                        build_downloaded_notification();
                        stop_service();

                    } catch (Exception e) {
                        Log.i("文件下载错误 ", e.getMessage());
                        throw new Exception("file download fail");
                    }
                } catch (Exception e) {
                    Log.i("下载有问题1 ", e.getMessage());
                }


                return null;
            }

            @Override
            protected void onProgressUpdate(Void...result) {
                Log.i("onUpdate 线程ID ", Thread.currentThread().toString());
                listener.on_update(file_downloader.downloaded_size);

            }


            @Override
            protected void onPostExecute(Void obj) {
                super.onPostExecute(obj);
            }
        }.execute();


        return this.file_downloader.downloaded_size;
    }



    private void init_connection(Context context,
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




    private void stop_service() {
        not_finish = false;

        notification_service_bar.stopForeground(notice_id);
        stopSelf();
    }

    private void save_thread_data() {
        try {
            RandomAccessFile rand_out = new RandomAccessFile(file_downloader.save_file, "rw");
            if(file_downloader.file_size >0) rand_out.setLength(file_downloader.file_size);
            rand_out.close();
            url = new URL(file_downloader.download_url);
            if(file_downloader.thread_data.size() != file_downloader.threads.length){
                file_downloader.thread_data.clear();
                for (int i = 0; i < file_downloader.threads.length; i++) {
                    file_downloader.thread_data.put(i+1, 0);
                }
            }
            for (int i = 0; i < file_downloader.threads.length; i++) {
                int downLength = file_downloader.thread_data.get(i+1);
                if(downLength < file_downloader.block && file_downloader.downloaded_size <file_downloader.file_size){
                    file_downloader.threads[i] = new DownloadThread(
                            DownloadService.this.file_downloader,
                            url,
                            file_downloader.save_file,
                            file_downloader.block,
                            file_downloader.thread_data.get(i+1),
                            i+1);

                    file_downloader.threads[i].setPriority(7);
                    file_downloader.threads[i].start();
                }else{
                    file_downloader.threads[i] = null;
                }
            }
            file_downloader.file_record.save(file_downloader.download_url,
                    file_downloader.thread_data);
        } catch (Exception e) {
            Log.i("保存线程数据错误 ", e.getMessage());
        }

    }

    private void build_downloaded_notification() {
        Intent in = new Intent("app.action.download_done_notification");
        in.putExtras(file_downloader.intent_extras);
        in.putExtra("activity_class", file_downloader.activity_class.getName());
        in.putExtra("filename", regenerate_filename(file_downloader.get_file_name()));
        in.putExtra("file_size", show_human_size(file_downloader.get_file_size()));
        getApplicationContext().sendBroadcast(in);
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



    public String show_human_size(long bytes) {
        Boolean si = true;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}