package com.mindpin.android.filedownloader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mindpin.android.filedownloader.R;


public class EntryActivity extends Activity {
    Button system_btn, other_btn;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        system_btn = (Button) findViewById(R.id.system_btn);
        system_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(EntryActivity.this, SystemDownloadActivity.class);
                startActivity(i);
            }
        });



        other_btn = (Button) findViewById(R.id.other_btn);
        other_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(EntryActivity.this, DownloadActivity.class);
                startActivity(i);
            }
        });
    }
}