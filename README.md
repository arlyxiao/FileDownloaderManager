FileDownloaderManager
=====================

FileDownloader library for android



### filedownload-samples: 

1 需要下载的文件的网络地址<br>

2 下载后文件的保存路径<br>

3 用几个线程进行下载<br>



### filedownload:

下载文件在 service 中运行，并且封装在组件内部

1 下载进度可以显示在通知栏 <br>

2 下载文件的程序最小化时，下载需要仍然继续 <br>

3 下载进行时，通知栏上显示了下载进度等信息




==================

### 组件的调用API

```java
public class FileDownloader{
  // 构造函数
  // context Context 实例，一般为一个 activity 实例
  // download_url 要下载的 URL
  // file_save_dir 下载后的文件保存的位置
  // thread_num  启动几个线程进行下载
  public FileDownloader(Context context, String download_url, File file_save_dir, int thread_num);
}
```

以上几个参数设置后，就可以给组件设置下载的监视钩子，并开始下载
```java
public class FileDownloader{
  // 开始下载
  // ProgressUpdateListener 监听下载数量的变化，listener 需要运行在UI线程
  // 如果不需要监听可以设置为 null
  public void download(ProgressUpdateListener listener);

  interface ProgressUpdateListener {
    public void on_update(int downloaded_size);
  }
}
```

开始下载后，需要可以获取到下载的文件的名称和大小
```java
public class FileDownloader{
  // 获取要下载的文件的大小(单位为字节)
  public int get_file_size();

  // 获取要下载的文件的名字
  public String get_file_name();

}
```

下载进行时，通知栏上显示了下载进度等信息，需要给通知栏信息注册一个点击事件，下面这个接口可以让组件的使用者设置点击时要打开的activity，在打开activity时带上 intent_extras 参数
```java
public void set_notification(Class activity_class, Bundle intent_extras);
```

然后下载任务可以暂停
```java
public void pause_download();
```

任务可以删除
```java
public void stop_download();
```


Activity onPause 取消广播
```java
public void unregister_download_receiver();
```


Activity onResume 激活广播
```java
public void register_download_receiver(ProgressUpdateListener listener);
```

```java
public class FileDownloader{
  // 构造函数
  // context Context 实例，一般为一个 activity 实例
  // download_url 要下载的 URL
  // file_save_dir 下载后的文件保存的位置
  // thread_num  启动几个线程进行下载
  public FileDownloader(Context context, String download_url, File file_save_dir, int thread_num);

  // 获取要下载的文件的大小(单位为字节)
  public int get_file_size();

  // 获取要下载的文件的名字
  public String get_file_name();

  // 开始下载
  // ProgressUpdateListener 监听下载数量的变化，listener 需要运行在UI线程
  // 如果不需要监听可以设置为 null
  public void download(ProgressUpdateListener listener);

  interface ProgressUpdateListener {
    public void on_update(int downloaded_size);
  }

  // 设置通知栏信息的点击事件行为
  public void set_notification(Class activity_class, Bundle intent_extras);

  public void pause_download();
  public void stop_download();
}
```

### 整体在 Activity 中的使用示例如下

```java
public class MainActivity extends Activity{
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    run_download_1();
    run_download_2();
  }

  public void run_download_1(){
    Context context = this;
    String url = "http://www.baidu.com/img/bdlogo.gif";
    File save_dir  = new File("/sd/files");
    FileDownloader fd = new FileDownloader(context, url, save_dir, 2);

    Bundle b = new Bundle();
    b.putString("param_name1", "param_value1");
    fd.set_notification(MainActivity.class, b);

    fd.download(new ProgressUpdateListener(){
      public void on_update(int downloaded_size){
        // 这个方法需要运行在UI线程
        // 比如这里增加逻辑:在主界面显示下载进度条
        // downloaded_size 单位是字节，表示已经下载了的字节数

        // 获取要下载的文件的大小(单位为字节)
        fd.get_file_size();

        // 获取要下载的文件的名字
        fd.get_file_name();
      }
    });
  }


  public void run_download_2(){
    Context context = this;
    String url = "http://www.google.com/img/logo.gif";
    File save_dir  = new File("/sd/files");
    FileDownloader fd1 = new FileDownloader(context, url, save_dir, 2);

    Bundle b = new Bundle();
    b.putString("param_name1", "param_value1");
    fd1.set_notification(MainActivity.class, b);

    fd1.download(new ProgressUpdateListener(){
      public void on_update(int downloaded_size){
        // 这个方法需要运行在UI线程
        // 比如这里增加逻辑:在主界面显示下载进度条
        // downloaded_size 单位是字节，表示已经下载了的字节数

        // 获取要下载的文件的大小(单位为字节)
        fd1.get_file_size();

        // 获取要下载的文件的名字
        fd1.get_file_name();
      }
    });
  }
}
```




AndroidManiFest 设置
```java
<service android:name="com.mindpin.android.filedownloader.DownloadService" />

<receiver
    android:name=".DownloadProgressNotificationWidget"
    android:label="DownloadProgressNotificationWidget" >
    <intent-filter>
        <action android:name="app.action.download_progress_notification_widget" />
    </intent-filter>

</receiver>


<receiver
    android:name=".DownloadDoneNotification"
    android:label="DownloadDoneNotification" >
    <intent-filter>
        <action android:name="app.action.download_done_notification" />
    </intent-filter>

</receiver>

<receiver
    android:name=".DownloadPauseReceiver"
    android:label="DownloadPauseReceiver" >
    <intent-filter>
        <action android:name="app.action.download_pause_receiver" />
    </intent-filter>

</receiver>

<receiver
    android:name=".DownloadStopReceiver"
    android:label="DownloadStopReceiver" >
    <intent-filter>
        <action android:name="app.action.download_stop_receiver" />
    </intent-filter>

</receiver>

<receiver
    android:name=".DownloadListenerReceiver"
    android:label="DownloadListenerReceiver" >
    <intent-filter>
        <action android:name="app.action.download_listener_receiver" />
    </intent-filter>

</receiver>
```

使用示例

```java
Context context = // 某个 activity 实例
String url = "http://www.baidu.com/img/bdlogo.gif";
File save_dir  = new File("/sd/files");
FileDownloader fd = new FileDownloader(context, url, save_dir, 2);

fd.download(new ProgressUpdateListener(){
  public void on_update(int downloaded_size){
    // 这个方法需要运行在UI线程
    // 比如这里增加逻辑:在主界面显示下载进度条
    // downloaded_size 单位是字节，表示已经下载了的字节数

  }
});

```

==========




### DownloadManager API 相关说明

1, 启动下载，并显示通知栏进度条信息, 多个任务也同时显示在通知栏上

```java
// 初始化下载 URL 路径
Uri uri = Uri.parse("http://esharedev.oss-cn-hangzhou.aliyuncs.com/file/%E5%9B%BE%E7%89%87%E6%94%BE%E5%A4%A7%E7%BC%A9%E5%B0%8F%E6%97%8B%E8%BD%AC.mp4");

DownloadManager.Request request = new Request(uri);

// 设置下载目录，文件名
request.setDestinationInExternalPublicDir("mindpin", "less_5mb.mp4");

// 设置只允许在WIFI的网络下下载
request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

// 加入下载队列, 开始下载
int download_id = downloadmanager.enqueue(request);
```

2, 自定义通知栏进度条信息点击事件

```java
// 启动 Activity onCreate 方法里激活广播通知
registerReceiver(on_notification_click,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));


// 自定义点击通知栏后要做的逻辑
BroadcastReceiver on_notification_click = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "通知栏点击提示", Toast.LENGTH_LONG).show();
        }
    };
```



3, 自定义下载完成通知栏信息事件

```java
// 启动 Activity onCreate 方法里激活广播通知
registerReceiver(on_complete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


// 自定义完成下载后的逻辑
BroadcastReceiver on_complete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "已经下载完成提示", Toast.LENGTH_LONG).show();
        }
    };
```


4, 删除下载任务

```java
// 初始化 downloadmanager
DownloadManager downloadmanager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

// download_id 为上面启动下载方法 downloadmanager.enqueue(request); 这里的返回值
downloadmanager.remove(download_id);
```


5, 自定义界面进度条, 提供监视下载进度变化的钩子方法，在钩子方法中自定义界面进度条
```java
// 需要使用到系统 ContentObserver
class DownloadChangeObserver extends ContentObserver {

    public DownloadChangeObserver() {
        // 这里的 handler 为自己定义的一个 Handler 类实例，用来处理更新进度条的相关数据操作
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        update_progress();
    }

}

// button onClick事件里激活
getContentResolver().registerContentObserver(CONTENT_URI, true, download_observer);

// 更新进度条所需要的数据
public void update_progress() {
    int[] bytes_and_status = new int[] {-1, -1, 0};
    DownloadManager.Query query = new DownloadManager.Query().setFilterById(download_id);
    Cursor c = null;
    try {
        c = downloadmanager.query(query);
        if (c != null && c.moveToFirst()) {
            bytes_and_status[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            Log.i("到目前为止下载的大小 ", Integer.toString(bytes_and_status[0]));
            bytes_and_status[1] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            Log.i("总大小 ", Integer.toString(bytes_and_status[0]));
            bytes_and_status[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            Log.i("下载状态 ", Integer.toString(bytes_and_status[0]));
        }
    } finally {
        if (c != null) {
            c.close();
        }
    }

    // 需要用到 Hanlder 类来处理这些数据信息显示到进度条上
    handler.sendMessage(handler.obtainMessage(0, bytes_and_status[0], bytes_and_status[1], bytes_and_status[2]));
}
```







