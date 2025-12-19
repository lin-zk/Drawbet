package com.example.majordesign_master_v1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about); // 将布局文件与关于页面关联

        TextView githubLink = findViewById(R.id.github_link);
        if (githubLink != null) {
            githubLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = getString(R.string.github_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }
}