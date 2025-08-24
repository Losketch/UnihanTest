package top.rainysummer.unihantest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView txtAppName = findViewById(R.id.txtAppName);
        TextView txtVersion = findViewById(R.id.txtVersion);
        TextView txtAuthor = findViewById(R.id.txtAuthor);
        TextView txtDescription = findViewById(R.id.txtDescription);
        
        Button btnGithub = findViewById(R.id.btnGithub);
        Button btnNotoFont = findViewById(R.id.btnNotoFont);
        Button btnUnicodeFont = findViewById(R.id.btnUnicodeFont);
        Button btnBack = findViewById(R.id.btnBack);

        btnGithub.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/Losketch/UnihanTest/"));
            startActivity(intent);
        });

        btnNotoFont.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.google.cn/get/noto/"));
            startActivity(intent);
        });

        btnUnicodeFont.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/Losketch/UnicodeFontSet-magisk-module/"));
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
