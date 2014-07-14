package com.mindpin.android.filedownloader;


public class DownloadStore {
    public int obj_id = 0;
    public boolean should_pause = false;
    public boolean should_stop = false;


    public DownloadStore(int obj_id) {
        this.obj_id = obj_id;
    }
}
