package com.mindpin.android.filedownloader;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class FileDownloader implements Parcelable  {
    public HttpURLConnection conn;
    public static Context context;
    public FileRecord file_record;
    /* 已下载文件长度 */
    public int downloaded_size = 0;
    /* 原始文件长度 */
    public int file_size = 0;

    /* 线程数 */
    public DownloadThread[] threads;
    public int thread_num;

    /* 本地保存文件 */
    public File save_file;
    /* 缓存各线程下载的长度*/
    public Map<Integer, Integer> thread_data = new ConcurrentHashMap<Integer, Integer>();
    /* 每条线程下载的长度 */
    public int block;
    /* 下载路径  */
    public String download_url;

    Class activity_class;
    Bundle intent_extras;

    public ProgressUpdateListener listener;

    URL url;
    boolean is_finished = false;
    public static Queue received_queue = new LinkedList();

//    public FileDownloader current = this;
//
//    Intent download_service;

    public boolean should_pause = false;
    public boolean should_stop = false;

    private int obj_id = 0;
    public int notice_id = new Random().nextInt(999999999);
    public long when = System.currentTimeMillis();

    // Intent download_service;






    @Override
    public int describeContents() {
        // TODO Auto-generated method stub

        return 0;
    }



    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FileDownloader createFromParcel(Parcel in) {
            return new FileDownloader(in);
        }

        public FileDownloader[] newArray(int size) {
            return new FileDownloader[size];
        }
    };


    public FileDownloader(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

//        Log.i("download_url, 测试输出 ", download_url);
        dest.writeString(download_url);
        dest.writeBundle(intent_extras);
        dest.writeInt(thread_num);
        dest.writeValue(save_file);
        dest.writeValue(activity_class);

        dest.writeByte((byte) (should_pause ? 1 : 0));
        dest.writeByte((byte) (should_stop ? 1 : 0));
        dest.writeInt(obj_id);
        dest.writeInt(notice_id);
        dest.writeInt(file_size);
        dest.writeInt(downloaded_size);
        dest.writeLong(when);
    }


    private void readFromParcel(Parcel in) {
        download_url = in.readString();
        intent_extras = in.readBundle();
        thread_num = in.readInt();
        save_file = (File)in.readValue(getClass().getClassLoader());
        activity_class = (Class)in.readValue(getClass().getClassLoader());
        should_pause = in.readByte() != 0;
        should_stop = in.readByte() != 0;
        obj_id = in.readInt();
        notice_id = in.readInt();
        file_size = in.readInt();
        downloaded_size = in.readInt();
        when = in.readLong();

    }



    public int get_obj_id() {
        return obj_id;
    }

    public void set_obj_id() {
        // this.obj_id = this.hashCode();
        this.obj_id = download_url.hashCode();
    }

    public int get_thread_size() {
        return threads.length;
    }





    public int get_file_size() {
//        try {
//            Log.i("bind 中传 obj_id 111 ", Integer.toString(obj_id));
//            Intent download_service = new Intent(context, DownloadService.class);
//            context.bindService(download_service, m_connection, Context.BIND_AUTO_CREATE);
//        } catch (Exception e) {
//            Log.i("bindService 错误 ", e.toString());
//            e.printStackTrace();
//        }

        return file_size;
    }


    protected synchronized void append(int size) {
        downloaded_size += size;
    }

    protected synchronized void update(int thread_id, int pos) {
        this.thread_data.put(thread_id, pos);
        this.file_record.update(this.download_url, this.thread_data);
    }


//    public FileDownloader() {
//    }


    public FileDownloader(Context context,
                          String download_url,
                          File file_save_dir,
                          int thread_num) {

        file_save_dir = create_dir(file_save_dir);
        this.context = context;
        this.download_url = download_url;
        this.save_file = file_save_dir;

        this.thread_num = thread_num;

        if (this.obj_id == 0) {
            set_obj_id();
            Log.i("生成 obj_id ", Integer.toString(obj_id));
        }

    }


//    public void recover() {
//        this.file_record = null;
//        this.downloaded_size = 0;
//        this.file_size = 0;
//        this.save_file = null;
//        this.block = 0;
//        this.thread_data = null;
//        this.threads = null;
//        this.thread_num = 0;
//        this.download_url = null;
//    }


    public void set_notification(Class activity_class, Bundle intent_extras) {
        this.activity_class = activity_class;
        this.intent_extras = intent_extras;
    }


    public String get_file_name() {
        String filename = this.download_url.substring(this.download_url.lastIndexOf('/') + 1);
        if(filename==null || "".equals(filename.trim())){
            for (int i = 0;; i++) {
                String mine = conn.getHeaderField(i);
                if (mine == null) break;
                if("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())){
                    Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
                    if(m.find()) return m.group(1);
                }
            }
            filename = UUID.randomUUID()+ ".tmp";
        }

        return filename;
    }


    private File create_dir(File file_dir) {
        file_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + file_dir.getPath());
        if (file_dir.exists()) {
            Log.i("目录已经存在 ", file_dir.getAbsolutePath());
            return file_dir;
        }

        Log.i("目录不存在 开始创建目录 ", "true");

        try{
            boolean result = file_dir.mkdirs();
            if (result) {
                Log.i("目录创建成功 ", "true");
                return file_dir;
            }
        } catch(SecurityException se){
            Log.i("目录创建失败 ", se.toString());
        }

        return null;
    }





    public static Map<String, String> get_http_response_header(HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null) break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }


    public static void print_response_header(HttpURLConnection http){
        Map<String, String> header = get_http_response_header(http);
        for(Map.Entry<String, String> entry : header.entrySet()){
            String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
            print(key+ entry.getValue());
        }
    }

    public static void print(String msg){
        Log.i("FileDownloader", msg);
    }


    public String get_test() {
        return "hello world";
    }





    public void download(final ProgressUpdateListener listener) throws Exception{

        NotificationServiceBar notification_service_bar =
                new NotificationServiceBar(context);

        notification_service_bar.
                wait_notification(this, notice_id);


        Intent download_service = new Intent(context, DownloadService.class);
        download_service.putExtra("download_manager", this);
        context.startService(download_service);


//        register_listener_receiver(listener);
//        register_done_receiver();
//        register_pause_receiver();
//        register_stop_receiver();

        register_download_receiver(listener);
    }


    BroadcastReceiver download_pause_receiver;
    BroadcastReceiver download_stop_receiver;
    BroadcastReceiver download_done_receiver;
    BroadcastReceiver progress_listener_receiver;


    private void register_pause_receiver() {
        download_pause_receiver = new DownloadPauseReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {

                fd = intent.getParcelableExtra("download_manager");

                if (fd.get_obj_id() == FileDownloader.this.get_obj_id()) {
                // if (fd.download_url == FileDownloader.this.download_url) {
                    Log.i("调试 pause 接收正在下载的 downloaded_size 值 ", Integer.toString(fd.downloaded_size));
                    FileDownloader.this.downloaded_size = 0;

                    FileDownloader.this.file_size = 0;
                    Log.i("调试 pause 接收正在下载的 file_size 值 ", Integer.toString(fd.file_size));
                } else {
                    Log.i("调试 广播接收到不同的 obj_id 3 ", Integer.toString(fd.get_obj_id()));
                }

            }
        };

        context.registerReceiver(download_pause_receiver,
                new IntentFilter("app.action.download_pause_receiver"));
    }

    private void unregister_pause_receiver() {
        if (download_pause_receiver == null) {
            return;
        }
        context.unregisterReceiver(download_pause_receiver);
    }

    private void register_stop_receiver() {
        download_stop_receiver = new DownloadStopReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {

                fd = intent.getParcelableExtra("download_manager");

                if (fd.get_obj_id() == FileDownloader.this.get_obj_id()) {
                // if (fd.download_url == FileDownloader.this.download_url) {
                    Log.i("调试 stop 接收正在下载的 downloaded_size 值 ", Integer.toString(fd.downloaded_size));
                    FileDownloader.this.downloaded_size = 0;

                    FileDownloader.this.file_size = 0;
                    Log.i("调试 stop 接收正在下载的 file_size 值 ", Integer.toString(fd.file_size));
                } else {
                    Log.i("调试 stop 广播接收到不同的 obj_id 4 ", Integer.toString(fd.get_obj_id()));
                }

            }
        };

        context.registerReceiver(download_stop_receiver,
                new IntentFilter("app.action.download_stop_receiver"));
    }

    private void unregister_stop_receiver() {
        if (download_stop_receiver == null) {
            return;
        }
        context.unregisterReceiver(download_stop_receiver);
    }


    private void register_done_receiver() {
        download_done_receiver = new DownloadDoneNotification() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {

                fd = intent.getParcelableExtra("download_manager");

                if (fd.get_obj_id() == FileDownloader.this.get_obj_id()) {
                // if (fd.download_url == FileDownloader.this.download_url) {
                    Log.i("调试 接收正在下载的 downloaded_size 值 ", Integer.toString(fd.downloaded_size));
                    FileDownloader.this.downloaded_size = 0;

                    FileDownloader.this.file_size = 0;
                    Log.i("调试 接收正在下载的 file_size 值 ", Integer.toString(fd.file_size));
                } else {
                    Log.i("调试 广播接收到不同的 obj_id 2 ", Integer.toString(fd.get_obj_id()));
                }

            }
        };

        context.registerReceiver(download_done_receiver,
                new IntentFilter("app.action.download_done_notification"));
    }

    private void unregister_done_receiver() {
        if (download_done_receiver == null) {
            return;
        }
        context.unregisterReceiver(download_done_receiver);
    }


    private void register_listener_receiver(final ProgressUpdateListener listener) {
        progress_listener_receiver = new DownloadListenerReceiver() {
            @Override
            public void onReceive(Context ctxt, Intent intent) {

                fd = intent.getParcelableExtra("download_manager");

                if (fd.get_obj_id() == FileDownloader.this.get_obj_id()) {
                // if (fd.download_url == FileDownloader.this.download_url) {
                    Log.i("接收正在下载的 downloaded_size 值 ", Integer.toString(fd.downloaded_size));
                    FileDownloader.this.downloaded_size = fd.downloaded_size;

                    FileDownloader.this.file_size = fd.file_size;

                    Log.i("接收正在下载的 file_size 值 ", Integer.toString(fd.file_size));

                    FileDownloader.this.listener = listener;
                    FileDownloader.this.listener.on_update(fd.downloaded_size);
                } else {
                    Log.i("调试 广播接收到不同的 obj_id 1 ", Integer.toString(fd.get_obj_id()));
                }

            }
        };

        context.registerReceiver(progress_listener_receiver,
                new IntentFilter("app.action.download_listener_receiver"));
    }

    private void unregister_listener_receiver() {
        if (progress_listener_receiver == null) {
            return;
        }
        context.unregisterReceiver(progress_listener_receiver);
    }

    public void unregister_download_receiver() {
        try {
            unregister_done_receiver();
            unregister_listener_receiver();
            unregister_pause_receiver();
            unregister_stop_receiver();
        } catch (Exception e) {
            Log.i("unregister 错误 ", "true");
            e.printStackTrace();
        }

    }


    public void register_download_receiver(ProgressUpdateListener listener) {
        try {
            register_pause_receiver();
            register_stop_receiver();
            register_done_receiver();
            register_listener_receiver(listener);
        } catch (Exception e) {
            Log.i("register 错误 ", "true");
            e.printStackTrace();
        }

    }



    public void pause_download() {


        // context.stopService(download_service);
        try {
            if (downloaded_size == 0) {
                return;
            }

            Log.i("调试 正在下载的大小 ", Integer.toString(downloaded_size));

            should_pause = true;

            Log.i("暂停下载 ", "true");
            Log.i("obj_id ", Integer.toString(this.hashCode()));

            Intent download_service = new Intent(context, DownloadService.class);

            download_service.putExtra("download_manager", this);
            context.startService(download_service);

            should_pause = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop_download() {

        should_stop = true;

        Log.i("停止下载 ", "true");
        Log.i("obj_id ", Integer.toString(this.hashCode()));
        Intent download_service = new Intent(context, DownloadService.class);
        download_service.putExtra("download_manager", this);
        context.startService(download_service);

        should_stop = false;

        NotificationManager nm =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        nm.cancel(notice_id);

    }


    DownloadService m_service;
    boolean m_bound = false;


//    private ServiceConnection m_connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
//            m_service = binder.getService();
//
//            Log.i("bind 中传 obj_id 222 ", Integer.toString(obj_id));
//            file_size = m_service.get_download_store(obj_id).file_size;
//            Log.i("bind 中传 file_size 222 ", Integer.toString(file_size));
//
//
//
//            m_bound = true;
//
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            // m_bound = false;
//            Log.i("下载完毕 ", "true");
//        }
//    };



    public void init_connection(Context context) {

//        try {
//            this.file_record = new FileRecord(context);
//        } catch (java.lang.VerifyError e) {
//            Log.i("VerifyError 错误 ", e.getMessage());
//            e.printStackTrace();
//        }

        if (intent_extras != null) {
            Log.i("intent_extras 取得值 ", intent_extras.getString("param_name1"));
            // return;
        }
        try {
            if (download_url == null) {
                Log.i("download_url 没有取到值 ", "true");
                Log.i("download_url 没有取到值 ", Integer.toString(thread_num));
                return;
            }

            if (save_file == null) {
                Log.i("save_file 没有取到值 ", "true");
                this.save_file = Environment.getExternalStorageDirectory();
                // return;
            }

            if (thread_num <= 0) {
                Log.i("thread_num 没有取到值 ", "true");
                return;
            }

            Log.i("下载的 URL ", download_url);
            Log.i("下载的 save_file path ", save_file.getAbsolutePath());
            Log.i("下载的 thread_num thread_num ", Integer.toString(thread_num));
            this.file_record = new FileRecord(context);
            Log.i("调试0 ", "true");
            URL url = new URL(this.download_url);
            if(!save_file.exists()) save_file.mkdirs();
            Log.i("调试1 ", "true");
            this.threads = new DownloadThread[thread_num];
            Log.i("调试2 ", "true");
            this.conn = (HttpURLConnection) url.openConnection();
            this.conn.setConnectTimeout(5 * 1000);
            this.conn.setRequestMethod("GET");
            this.conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            this.conn.setRequestProperty("Accept-Language", "zh-CN");
            this.conn.setRequestProperty("Referer", download_url);
            this.conn.setRequestProperty("Charset", "UTF-8");
            this.conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            this.conn.setRequestProperty("Connection", "Keep-Alive");
            this.conn.connect();
            Log.i("调试3 ", "true");
            this.print_response_header(this.conn);
            if (this.conn.getResponseCode()==200) {
                this.file_size = this.conn.getContentLength();
                Log.i("取得的文件大小 ", Integer.toString(file_size));
                Log.i("初始化连接 文件大小 ", Integer.toString(this.file_size));
                if (this.file_size <= 0) throw new RuntimeException("Unkown file size ");

                String filename = this.get_file_name();
                this.save_file = new File(save_file, filename);
                Map<Integer, Integer> logdata = this.file_record.get_data(download_url);
                if(logdata.size()>0){
                    for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
                        this.thread_data.put(entry.getKey(), entry.getValue());
                }
                if(this.thread_data.size()==this.threads.length){
                    for (int i = 0; i < this.threads.length; i++) {
                        this.downloaded_size += this.thread_data.get(i+1);
                    }
                    this.print("已经下载的长度mmm "+ this.downloaded_size);
                }
                //计算每条线程下载的数据长度
                this.block = (this.file_size % this.threads.length)==0?
                        this.file_size / this.threads.length :
                        this.file_size / this.threads.length + 1;
                Log.i("每条线程下载的数据长度 ", Integer.toString(block));
            }else{
                throw new RuntimeException("server no response ");
            }
        } catch (Exception e) {
            Log.i("下载错误 ", e.getMessage());
            e.printStackTrace();
        }
    }



    public void save_thread_data() {

        try {
            Log.i("保存调试0 ", this.save_file.getAbsolutePath());
            RandomAccessFile rand_out = new RandomAccessFile(this.save_file, "rw");
            Log.i("保存调试000 ", "true");
            if(this.file_size >0) rand_out.setLength(this.file_size);
            rand_out.close();
            Log.i("保存调试1 ", "true");
            url = new URL(this.download_url);
            Log.i("保存调试2 ", "true");
            if(this.thread_data.size() != this.threads.length){
                Log.i("保存调试3 ", "true");
                this.thread_data.clear();
                for (int i = 0; i < this.threads.length; i++) {
                    Log.i("保存调试4 ", "true");
                    this.thread_data.put(i+1, 0);
                }
            }
            Log.i("保存调试5 ", "true");
            if (this.block <= 0) {
                Log.i("保存调试6 ", "true");
            }

            if (this.threads == null) {
                Log.i("保存调试7 ", "true");
            }

            if (this.thread_data == null) {
                Log.i("保存调试8 ", "true");
            }

            if (this.downloaded_size <= 0) {
                Log.i("保存调试9 ", "true");
            }

            if (this.file_size <= 0) {
                Log.i("保存调试10 ", "true");
            }


            for (int i = 0; i < this.threads.length; i++) {
                int downLength = this.thread_data.get(i+1);

                Log.i("downLength 大小 ", Integer.toString(downLength));
                Log.i("block 大小 ", Integer.toString(block));
                Log.i("downloaded_size 大小 ", Integer.toString(downloaded_size));
                Log.i("file_size 大小 ", Integer.toString(file_size));

                if(downLength < this.block && this.downloaded_size <this.file_size){
                    Log.i("开始进行线程下载 ", "true");
                    this.threads[i] = new DownloadThread(
                            this,
                            url,
                            this.save_file,
                            this.block,
                            this.thread_data.get(i+1),
                            i+1);

                    this.threads[i].setPriority(7);
                    this.threads[i].start();
                }else{
                    this.threads[i] = null;
                }
            }
            this.file_record.save(this.download_url,
                    this.thread_data);
        } catch (Exception e) {
            Log.i("保存线程数据错误 ", e.getMessage());
        }

    }


    public void continue_download_with_thread() {

        for (int i = 0; i < this.threads.length; i++){

            if (this.threads == null) {
                Log.i("threads 为空  ", "true");
            }
            Log.i("threads length ", Integer.toString(this.threads.length));
            // Log.i("thread id ", Long.toString(this.threads[i].getId()));


            if (should_pause || should_stop) {
                Log.i("service中的线程可以停止loop了 ", "true");
                return;
            }

            if (this.threads[i] != null && !this.threads[i].is_finish()) {
                is_finished = false;
                Log.i("一直在下载 000 ", "true");
                if (this.threads[i].isInterrupted()) {
                    this.threads[i].start();
                    Log.i("thread 启动中 ", "true");
                }
                if(this.threads[i].get_downloaded_length() == -1){
                    Log.i("一直在下载 111 ", "true");
                    this.threads[i] = new DownloadThread(
                            this,
                            url,
                            this.save_file,
                            this.block,
                            this.thread_data.get(i+1), i+1);
                    this.threads[i].setPriority(7);
                    this.threads[i].start();
                }
            }

        }

    }




}