package com.mindpin.android.filedownloader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class TargetActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();

        String param_name1 = extras.getString("param_name1");
        Log.i("从通知栏获取到的 ", param_name1);
    }
}