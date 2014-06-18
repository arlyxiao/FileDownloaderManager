package com.mindpin.android.filedownloader;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;


public class FileDownloader {
    private HttpURLConnection conn;
    private static final String TAG = "FileDownloader";
    private Context context;
    private FileRecord file_record;
    /* 已下载文件长度 */
    private int downloaded_size = 0;
    /* 原始文件长度 */
    private int file_size = 0;
    /* 线程数 */
    private DownloadThread[] threads;
    /* 本地保存文件 */
    private File save_file;
    /* 缓存各线程下载的长度*/
    private Map<Integer, Integer> thread_data = new ConcurrentHashMap<Integer, Integer>();
    /* 每条线程下载的长度 */
    private int block;
    /* 下载路径  */
    private String download_url;


    public int get_thread_size() {
        return threads.length;
    }


    public int get_file_size() {
        return file_size;
    }


    protected synchronized void append(int size) {
        downloaded_size += size;
    }

    protected synchronized void update(int thread_id, int pos) {
        this.thread_data.put(thread_id, pos);
        this.file_record.update(this.download_url, this.thread_data);
    }


    public FileDownloader(Context context, String download_url, File file_save_dir, int thread_num) {
        try {
            Log.i("下载的 URL ", download_url);
            this.context = context;
            this.download_url = download_url;
            file_record = new FileRecord(this.context);
            URL url = new URL(this.download_url);
            if(!file_save_dir.exists()) file_save_dir.mkdirs();
            this.threads = new DownloadThread[thread_num];
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5*1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty("Referer", download_url);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            print_response_header(conn);
            if (conn.getResponseCode()==200) {
                this.file_size = conn.getContentLength();
                if (this.file_size <= 0) throw new RuntimeException("Unkown file size ");

                String filename = get_file_name();
                this.save_file = new File(file_save_dir, filename);
                Map<Integer, Integer> logdata = file_record.get_data(download_url);
                if(logdata.size()>0){
                    for(Map.Entry<Integer, Integer> entry : logdata.entrySet())
                        thread_data.put(entry.getKey(), entry.getValue());
                }
                if(this.thread_data.size()==this.threads.length){
                    for (int i = 0; i < this.threads.length; i++) {
                        this.downloaded_size += this.thread_data.get(i+1);
                    }
                    print("已经下载的长度 "+ this.downloaded_size);
                }
                //计算每条线程下载的数据长度
                this.block = (this.file_size % this.threads.length)==0? this.file_size / this.threads.length : this.file_size / this.threads.length + 1;
            }else{
                throw new RuntimeException("server no response ");
            }
        } catch (Exception e) {
            print(e.toString());
            Log.i("下载错误 ", e.getMessage());
            throw new RuntimeException("don't connection this url");
        }
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



    public int download(ProgressUpdateListener listener) throws Exception{
        try {
            RandomAccessFile rand_out = new RandomAccessFile(this.save_file, "rw");
            if(this.file_size >0) rand_out.setLength(this.file_size);
            rand_out.close();
            URL url = new URL(this.download_url);
            if(this.thread_data.size() != this.threads.length){
                this.thread_data.clear();
                for (int i = 0; i < this.threads.length; i++) {
                    this.thread_data.put(i+1, 0);
                }
            }
            for (int i = 0; i < this.threads.length; i++) {
                int downLength = this.thread_data.get(i+1);
                if(downLength < this.block && this.downloaded_size <this.file_size){
                    this.threads[i] = new DownloadThread(this, url, this.save_file, this.block, this.thread_data.get(i+1), i+1);
                    this.threads[i].setPriority(7);
                    this.threads[i].start();
                }else{
                    this.threads[i] = null;
                }
            }
            this.file_record.save(this.download_url, this.thread_data);
            boolean not_finish = true;
            while (not_finish) {
                Thread.sleep(900);
                not_finish = false;
                for (int i = 0; i < this.threads.length; i++){
                    if (this.threads[i] != null && !this.threads[i].is_finish()) {
                        not_finish = true;
                        if(this.threads[i].get_downloaded_length() == -1){
                            this.threads[i] = new DownloadThread(this, url, this.save_file, this.block, this.thread_data.get(i+1), i+1);
                            this.threads[i].setPriority(7);
                            this.threads[i].start();
                        }
                    }
                }
                if(listener!=null) listener.on_update(this.downloaded_size);
            }
            file_record.delete(this.download_url);
        } catch (Exception e) {
            print(e.getMessage());
            throw new Exception("file download fail");
        }
        return this.downloaded_size;
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

    private static void print(String msg){
        Log.i(TAG, msg);
    }
}