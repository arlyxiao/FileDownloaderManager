package com.mindpin.android.filedownloader;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.net.URL;
import java.util.ArrayList;


public class DownloadService extends Service {
    Context context;
    Boolean should_stop_foreground = false;
    // URL url;
    ArrayList<FileDownloader> download_store_list = new ArrayList<FileDownloader>();
    // ArrayList<Integer> file_task_threads = new ArrayList<Integer>();

    public int file_size;


    @Override
    public void onCreate() {
        Log.i("开始运行 download service ", "true");

        context = getApplicationContext();
    }

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i("服务开始启动了 ", "true");


        FileDownloader download_manager =
                intent.getParcelableExtra("download_manager");
        Log.i("传整个对象给 service, 测试输出 ", download_manager.get_test());

        Log.i("对象 has_code ", Integer.toString(download_manager.obj_id));
        if (download_manager.should_stop) {
            Log.i("调试 应该要停止 ", "true");
        }

        if (download_manager.should_pause) {
            Log.i("调试 应该要暂停 ", "true");
        }


//        if (get_download_store(download_manager.obj_id) == null ||
//                (download_manager.should_pause == false &&
//                download_manager.should_stop == false)
//                ) {
//            save_download_manager(download_manager);
//
//
//            FileTaskThread file_task_thread =
//                    new FileTaskThread(intent, download_manager, download_manager.notice_id);
//            file_task_thread.run();
//
//
//
//            Log.i("初始化 thread ", "true");
//        }
        // save_download_manager(download_manager);

        FileTaskThread file_task_thread =
                new FileTaskThread(intent, download_manager, download_manager.notice_id);
        file_task_thread.run();













        should_stop_foreground = intent.getBooleanExtra("should_stop_foreground", false);
        if (should_stop_foreground) {
            Log.i("需要把服务放到后台运行 ", "true");

            // stop_forground_notification();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i("下载服务关闭 ", "true");

    }



    @Override
    public IBinder onBind(Intent intent) {

        return m_binder;
    }

//    @Override
//    public boolean onUnbind(Intent intent) {
//
//        Log.i("服务解决绑定callback ", "true");
//
//        FileDownloader download_manager =
//                intent.getParcelableExtra("download_manager");
//        Log.i("传整个对象给 service, 测试输出 ", download_manager.get_test());
//
//        FileTaskThread file_task_thread =
//                new FileTaskThread(intent, download_manager, download_manager.notice_id);
//        file_task_thread.run();
//
//        stop_service();
//        clear_local_thread_data(download_manager);
//        clear_notice_bar(download_manager.notice_id);
//
//        return true;
//    }



    private final IBinder m_binder = new LocalBinder();



    private void clear_notice_bar(int notice_id) {
        NotificationServiceBar notification_service_bar =
                new NotificationServiceBar(getApplicationContext(),
                        DownloadService.this);

        notification_service_bar.stop_foreground(notice_id);
    }

    private void stop_service() {

        DownloadService.this.stopSelf();
    }


    private void build_download_done_notification(FileDownloader file_downloader) {

        Log.i("通知的文件大小 ", Integer.toString(file_downloader.get_file_size()));
        Intent in = new Intent("app.action.download_done_notification");
        if (file_downloader.intent_extras != null) {
            in.putExtras(file_downloader.intent_extras);
        }

        if (file_downloader.activity_class != null) {
            in.putExtra("activity_class", file_downloader.activity_class.getName());
        }

        in.putExtra("filename", regenerate_filename(file_downloader.get_file_name()));
        in.putExtra("file_size", show_human_size(file_downloader.get_file_size()));
        getApplicationContext().sendBroadcast(in);
    }

    private void clear_local_thread_data(FileDownloader file_downloader) {

        if (file_downloader.download_url == null) {
            Log.i("清理 cache 数据 download_url 为空　", "true");
        }

        if (file_downloader.file_record == null) {
            Log.i("清理 cache 数据 file_record 为空　", "true");
            return;
        }
        try {
            file_downloader.file_record.delete(file_downloader.download_url);
            Log.i("清理 cache 数据　", "true");
        } catch (Exception e) {
            Log.i("清理 cache 数据错误 ", e.toString());
            e.printStackTrace();
        }

    }


//    private void stop_forground_notification() {
//        Intent i = new Intent(file_downloader.context, file_downloader.activity_class);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.putExtras(file_downloader.intent_extras);
//        startActivity(i);
//
//
//        notification_service_bar.stop_foreground(notice_id);
//
//        NotificationManager notice_manager =
//                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        notice_manager.cancelAll();
//    }

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



    private class FileTaskThread extends Thread {
        Intent intent;
        FileDownloader download_manager;
        int notice_id;

        public FileTaskThread(Intent intent,
                              FileDownloader download_manager,
                              int notice_id) {
            this.intent = intent;
            this.download_manager = download_manager;
            Log.i("notice_id值 ", Integer.toString(notice_id));
            this.notice_id = notice_id;

            if (download_manager.should_pause || download_manager.should_stop) {
                this.setPriority(MAX_PRIORITY);
                Log.i("设置成最大优先级 ", "true");
            }
        }

        @Override
        public void run() {
            final int obj_id = download_manager.obj_id;

            final NotificationServiceBar notification_service_bar =
                    new NotificationServiceBar(getApplicationContext(),
                            DownloadService.this);

            if (get_download_store(download_manager.obj_id) == null) {
                notification_service_bar.
                        wait_notification(download_manager, notice_id);
            }


            save_download_manager(download_manager);

//            if (!download_manager.should_pause && !download_manager.should_stop) {
//                notification_service_bar.
//                        wait_notification(download_manager, notice_id);
//            }



                new AsyncTask<Void, FileDownloader, Void>() {
                    @Override
                    protected Void doInBackground(Void... objects) {

                        try {

                            download_manager.init_connection(context);
                            file_size = download_manager.file_size;
                            download_manager.save_thread_data();

                            download_manager.is_finished = false;
                            while (!download_manager.is_finished) {
                                Thread.sleep(900);

                                // 停止下载
                                if (get_download_store(obj_id).should_stop) {
                                    download_manager.should_stop = true;
                                    Log.i("should_stop true", "true");
                                    return null;
                                } else {
                                    Log.i("should_stop false", "false");
                                }

                                // 暂停下载
                                if (get_download_store(obj_id).should_pause) {
                                    download_manager.should_pause = true;
                                    Log.i("should_pause为 true", "true");
                                    return null;
                                } else {
                                    Log.i("should_pause为 false", "false");
                                }



                                download_manager.is_finished = true;
                                download_manager.continue_download_with_thread();


                                notification_service_bar.
                                        handle_notification(download_manager, notice_id);

                                if (download_manager.listener != null) {
                                    Log.i("从 service 中传 listener 进度条 ", "true");
                                    publishProgress(download_manager);
                                }


                            }

                            return null;

                        } catch (Exception e) {
                            Log.i("下载有错误 ", e.toString());
                            e.printStackTrace();
                        }


                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(FileDownloader... result) {
                        FileDownloader download_manager = result[0];
                        Log.i("onUpdate 线程ID ", Thread.currentThread().toString());

                        download_manager.listener.on_update(download_manager.downloaded_size);
                    }

                    @Override
                    protected void onPostExecute(Void result) {

                        if (download_manager.should_stop) {
                            Log.i("整个停止下载 ", "true");
                            clear_notice_bar(notice_id);
                            clear_local_thread_data(download_manager);
                            stop_service();
                            return;
                        }

                        if (download_manager.should_pause) {
                            Log.i("线程停止 ", "true");
                            return;
                        }

                        build_download_done_notification(download_manager);
                        clear_notice_bar(notice_id);
                        clear_local_thread_data(download_manager);
                        stop_service();
                    }
                }.execute();

//            } finally {
//                Log.i("FileTaskThread 终止 ", "true");
////                notification_service_bar.
////                        handle_notification(download_manager, notice_id);
//            }



        }
    }


    public FileDownloader get_download_store(int obj_id) {
        if (download_store_list == null) {
            Log.i("download_store_list为 null ", "true");
            return null;
        }
        for (FileDownloader item : download_store_list) {
            if (item.obj_id == obj_id) {
                return item;
            }
        }
        return null;
    }

    private void save_download_manager(FileDownloader fd) {
        int obj_id = fd.obj_id;

        FileDownloader download_store = get_download_store(obj_id);

        if (download_store == null) {
            Log.i("第一次保存 obj_id ", "true");
            // download_store = new FileDownloader();
            download_store_list.add(fd);
        } else {
            download_store_list.remove(download_store);
            download_store_list.add(fd);
        }

        if (fd.should_pause) {
            Log.i("存储暂停 ", "true");
        }

        // download_store.should_pause = fd.should_pause;

        if (fd.should_stop) {
            Log.i("存储停止 ", "true");
        }
        // download_store.should_stop = fd.should_stop;
    }




}