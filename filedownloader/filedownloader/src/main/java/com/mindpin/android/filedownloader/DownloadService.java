package com.mindpin.android.filedownloader;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.RandomAccessFile;
import java.net.URL;


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
                if(listener!=null) listener.on_update(this.file_downloader.downloaded_size);
            }
            this.file_downloader.file_record.delete(this.file_downloader.download_url);
        } catch (Exception e) {
            Log.i("文件下载错误 ", e.getMessage());
            throw new Exception("file download fail");
        }
        return this.file_downloader.downloaded_size;
    }


    @Override
    public IBinder onBind(Intent intent) {

        return m_binder;
    }

    private final IBinder m_binder = new LocalBinder();
}