package com.mindpin.android.filedownloader;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.Serializable;

public interface ProgressUpdateListener  {
//    @Override
//    public int describeContents() {
//        // TODO Auto-generated method stub
//
//        return 0;
//    }
//
//
//
//    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
//        public ProgressUpdateListener createFromParcel(Parcel in) {
//            return new ProgressUpdateListener(in);
//        }
//
//        public ProgressUpdateListener[] newArray(int size) {
//            return new ProgressUpdateListener[size];
//        }
//    };
//
//    public ProgressUpdateListener(Parcel in) {  }
//
//
//
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//    }
//
//
//    public ProgressUpdateListener() {  }


    public void on_update(int downloaded_size);
}

