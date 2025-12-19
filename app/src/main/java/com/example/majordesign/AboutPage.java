package com.example.majordesign;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

public class AboutPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about); // 将布局文件与关于页面关联

        // 当用户点击链接时，使用显式 Intent 打开浏览器访问 GitHub 项目页面
        TextView githubLink = findViewById(R.id.github_link);
        if (githubLink != null) {
            githubLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = getString(R.string.github_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    // 如果有能够处理该 Intent 的应用，则启动
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }
}
