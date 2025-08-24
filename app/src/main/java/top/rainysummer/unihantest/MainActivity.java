package top.rainysummer.unihantest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUnihanTest = findViewById(R.id.btnUnihanTest);
        Button btnUnicodeTest = findViewById(R.id.btnUnicodeTest);
        Button btnAbout = findViewById(R.id.btnAbout);

        btnUnihanTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UnihanTestActivity.class);
            startActivity(intent);
        });

        btnUnicodeTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UnicodeTestActivity.class);
            startActivity(intent);
        });

        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }
}
