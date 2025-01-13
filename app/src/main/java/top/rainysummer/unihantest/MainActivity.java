package top.rainysummer.unihantest;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private int numValid = 0, numMax = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Thread(() -> {
            try {
                testUnicode();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void testUnicode() throws IOException, InterruptedException {
        InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open("Unihan.txt"));
        BufferedReader bufReader = new BufferedReader(inputReader);
        String line;
        while ((line = bufReader.readLine()) != null) {
            if (line.startsWith("#") || line.equals("")) {
                continue;
            }
            String finalLine = line;
            runOnUiThread(() -> updateUI(finalLine));
            //noinspection BusyWait
            Thread.sleep(1);
        }
        runOnUiThread(() -> {
            TextView textView = MainActivity.this.findViewById(R.id.textView);
            TextView textView3 = MainActivity.this.findViewById(R.id.textView3);
            textView.setVisibility(View.GONE);
            textView3.setVisibility(View.GONE);
        });
    }

    private boolean validEmoji(String unicode) {
        if (unicode.equals("&#x1F1F9&#x1F1FC")) {
            return true;
        }
        Paint paint = new Paint();
        boolean hasGlyph = paint.hasGlyph(String.valueOf(Html.fromHtml(unicode)));
        if (hasGlyph) {
            if (unicode.contains("&#x200D&#x")) {
                String strN = unicode.replaceAll("&#x200D&#x", "&#x");
                String strN2 = String.valueOf(Html.fromHtml(strN));
                String strN1 = String.valueOf(Html.fromHtml(unicode));
                int nL = strN1.length();
                int oL = strN2.length();
                //noinspection RedundantIfStatement
                if (nL != oL) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(String line) {
        numMax++;
        String formatU = MainActivity.this.formatUnicode(line);

        TextView textView2 = MainActivity.this.findViewById(R.id.textView2);
        TextView textView = MainActivity.this.findViewById(R.id.textView);
        TextView textView3 = MainActivity.this.findViewById(R.id.textView3);
        TextView textView5 = MainActivity.this.findViewById(R.id.textView5);
        textView.setText(Html.fromHtml(formatU));

        if (validEmoji(formatU)) {
            numValid++;
        }

        @SuppressLint("DefaultLocale") String percentage = String.format("%.2f", ((double) numValid / numMax) * 100);
        textView2.setText(numValid + " / " + numMax + " = ");
        String strDisplay = formatU.replace("&#x", " ");
        textView3.setText(strDisplay);
        textView5.setText(percentage + "%");

        TextView textView4 = MainActivity.this.findViewById(R.id.textView4);
        textView4.setText(numMax + " / 103367");
        ProgressBar progressBar = MainActivity.this.findViewById(R.id.progressBar);
        progressBar.setProgress(numMax);

        progressBar.invalidate();
        textView.invalidate();
        textView2.invalidate();
        textView3.invalidate();
        textView5.invalidate();
    }

    private String formatUnicode(String line) {
        String result = "";
        result += line.replaceAll("\\s.*", "");
        result = result.replaceAll("U+.", "&#x");
        return result;
    }
}