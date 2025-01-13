package top.rainysummer.unihantest;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private int numValid = 0, numMax = 0;
    private int maxProgress;
    private final Paint paint = new Paint();
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private ProgressBar progressBar;
    private final Handler handler = new Handler();
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final double EPSILON = 0.0001;
    private static final Map<Double, String> GRADE_THRESHOLDS = new LinkedHashMap<Double, String>() {{
        put(100.0, "φ");
        put(96.0, "V");
        put(92.0, "S");
        put(88.0, "A");
        put(82.0, "B");
        put(70.0, "C");
        put(64.0, "D");
        put(40.0, "E");
    }};

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        maxProgress = progressBar.getMax();
        executorService.execute(this::testUnicode);
    }

    private void initViews() {
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        progressBar = findViewById(R.id.progressBar);
    }

    private void testUnicode() {
        try (InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open("Unihan.txt"));
             BufferedReader bufReader = new BufferedReader(inputReader)) {
            String line;
            while ((line = bufReader.readLine()) != null) {
                if (line.startsWith("#") || line.equals("")) {
                    continue;
                }
                String finalLine = line;
                handler.post(() -> updateUI(finalLine));
                Thread.sleep(1);
            }
            handler.post(() -> {
                textView3.setVisibility(View.GONE);
                updateFinalStatus();
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean validEmoji(String unicode) {
        if (unicode.equals("&#x1F1F9&#x1F1FC")) {
            return true;
        }
        boolean hasGlyph = paint.hasGlyph(String.valueOf(Html.fromHtml(unicode)));
        if (hasGlyph) {
            if (unicode.contains("&#x200D&#x")) {
                String strN = unicode.replaceAll("&#x200D&#x", "&#x");
                String strN2 = String.valueOf(Html.fromHtml(strN));
                String strN1 = String.valueOf(Html.fromHtml(unicode));
                int nL = strN1.length();
                int oL = strN2.length();
                return nL != oL;
            } else {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(String line) {
        numMax++;
        String formatU = formatUnicode(line);

        textView.setText(Html.fromHtml(formatU));

        if (validEmoji(formatU)) {
            numValid++;
        }

        @SuppressLint("DefaultLocale") String percentage = String.format("%.3f", ((double) numValid / numMax) * 100);
        textView2.setText(numValid + " / " + numMax + " = ");
        String strDisplay = formatU.replace("&#x", " ");
        textView3.setText(strDisplay);
        textView5.setText(percentage + "%");

        textView4.setText(numMax + " / " + maxProgress);
        progressBar.setProgress(numMax);
    }

    private void updateFinalStatus() {
        if (textView == null) return;

        double percentage = ((double) numValid / numMax) * 100;
        String grade = calculateGrade(percentage);
        textView.setText(grade);
    }

    private String calculateGrade(double percentage) {
        for (Map.Entry<Double, String> entry : GRADE_THRESHOLDS.entrySet()) {
            if (Math.abs(percentage - 100.0) < EPSILON) {
                return "φ";
            }
            if (percentage >= entry.getKey()) {
                return entry.getValue();
            }
        }
        return "F";
    }

    private String formatUnicode(String line) {
        return line.replaceAll("\\s.*", "").replace("U+", "&#x");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}