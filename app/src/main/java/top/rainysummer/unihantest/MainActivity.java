package top.rainysummer.unihantest;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final double EPSILON = 0.0001;
    private static final String SPECIAL_UNICODE = "&#x1F1E8&#x1F1F3";
    private static final String ZWJ_SEQUENCE = "&#x200D&#x";
    private static final int BATCH_SIZE = 80;
    private static final String BLOCK_PREFIX = "#BLOCK:";

    private int numValid = 0;
    private int numTotal = 0;
    private Map<String, BlockStatistics> blockStats = new HashMap<>();
    private String currentBlock = "Unknown";

    private Paint paint;
    private TextView textView;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private ProgressBar progressBar;
    private LinearLayout blockResultsContainer;
    private ScrollView scrollView;

    private Handler mainHandler;
    private ExecutorService executorService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        paint = new Paint();
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();

        progressBar.setMax(1000); // 先设置一个大数，后面根据实际调整

        executorService.execute(this::processUnicodeFile);
    }

    private void initViews() {
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        textView5 = findViewById(R.id.textView5);
        progressBar = findViewById(R.id.progressBar);
        blockResultsContainer = findViewById(R.id.blockResultsContainer);
        scrollView = findViewById(R.id.scrollView);
    }

    private void processUnicodeFile() {
        int maxProgress = 0;
        try (InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open("Unihan.txt"));
             BufferedReader bufferedReader = new BufferedReader(inputReader)) {

            String line;
            int batchCount = 0;
            StringBuilder batchBuilder = new StringBuilder();
            int lineCount = 0;

            // —— 第一次遍历：只统计非空、非注释行 的数量 —— //
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                // 忽略空行和#开头的行
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                lineCount++;
            }
            // 设置进度条最大值
            progressBar.setMax(lineCount);

            // 重新打开文件
            inputReader.close();
            try (InputStreamReader reReader = new InputStreamReader(getResources().getAssets().open("Unihan.txt"));
                 BufferedReader reBuffered = new BufferedReader(reReader)) {

                while ((line = reBuffered.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    // 处理区块标记
                    if (line.startsWith(BLOCK_PREFIX)) {
                        // 如果当前有区块在处理，更新其结果
                        if (blockStats.containsKey(currentBlock)) {
                            postBlockUpdate(currentBlock);
                        }

                        // 设置新的当前区块
                        currentBlock = line.substring(BLOCK_PREFIX.length()).trim();
                        // 兼容 API<24 的 putIfAbsent
                        CompatUtils.mapPutIfAbsent(blockStats, currentBlock, new BlockStatistics());
                        continue;
                    }

                    if (line.startsWith("#")) {
                        continue;
                    }

                    numTotal++;
                    BlockStatistics stats = blockStats.get(currentBlock);
                    stats.total++;

                    String formattedUnicode = formatUnicode(line);
                    if (isValidEmoji(formattedUnicode)) {
                        numValid++;
                        stats.valid++;
                    }

                    batchBuilder.append(formattedUnicode).append("\n");
                    batchCount++;

                    if (batchCount % BATCH_SIZE == 0) {
                        String batchText = batchBuilder.toString();
                        postUpdateUI(batchText);
                        batchBuilder.setLength(0);
                    }
                }

                // 处理剩余未满批次的内容
                if (batchBuilder.length() > 0) {
                    postUpdateUI(batchBuilder.toString());
                }

                // 更新最后一个区块的结果
                postBlockUpdate(currentBlock);
                maxProgress = numTotal;
            }

        } catch (IOException e) {
            e.printStackTrace();
            postError("读取文件失败：" + e.getMessage());
        }

        final int finalMaxProgress = maxProgress;
        mainHandler.post(() -> {
            progressBar.setMax(finalMaxProgress);
            progressBar.setProgress(finalMaxProgress);
            textView3.setVisibility(View.GONE);
            updateFinalStatus();
        });
    }

    private void postBlockUpdate(String blockName) {
        mainHandler.post(() -> {
            updateBlockUI(blockName);
        });
    }

    private void postUpdateUI(@NonNull String batchText) {
        mainHandler.post(() -> updateUI(batchText));
    }

    private void postError(String errorMsg) {
        mainHandler.post(() -> {
            textView.setText(errorMsg);
            textView5.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        });
    }

    private boolean isValidEmoji(String unicodeHtml) {
        if (SPECIAL_UNICODE.equals(unicodeHtml)) {
            return true;
        }

        Spanned sp = CompatUtils.fromHtml(unicodeHtml);
        String parsed = sp.toString();

        // Paint.hasGlyph() 加了版本判断
        if (!CompatUtils.hasGlyph(paint, parsed)) {
            return false;
        }

        if (unicodeHtml.contains(ZWJ_SEQUENCE)) {
            String replaced = unicodeHtml.replace(ZWJ_SEQUENCE, "&#x");
            Spanned sp2 = CompatUtils.fromHtml(replaced);
            String replacedStr = sp2.toString();
            return parsed.length() != replacedStr.length();
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void updateUI(String batchLines) {
        if (batchLines.isEmpty()) return;

        String[] lines = batchLines.split("\n");
        String lastLine = lines[lines.length - 1];

        textView.setText(CompatUtils.fromHtml(lastLine));
        textView2.setText(numValid + " / " + numTotal + " = ");
        textView3.setText(lastLine.replace("&#x", " "));
        textView5.setVisibility(View.GONE);

        textView4.setText(numTotal + " / " + progressBar.getMax());
        progressBar.setProgress(numTotal);
    }

    @SuppressLint("DefaultLocale")
    private void updateBlockUI(String blockName) {
        BlockStatistics stats = blockStats.get(blockName);
        if (stats == null) return;

        // 创建或更新区块的结果视图
        TextView blockView = findOrCreateBlockView(blockName);
        double percentage = stats.total > 0 ? ((double) stats.valid / stats.total) * 100 : 0;
        String grade = Grade.fromScore(percentage);

        blockView.setText(String.format("%s:\n %s - %d/%d (%.2f%%)",
                blockName, grade, stats.valid, stats.total, percentage));
    }

    private TextView findOrCreateBlockView(String blockName) {
        // 查找是否已有该区块的视图
        for (int i = 0; i < blockResultsContainer.getChildCount(); i++) {
            View child = blockResultsContainer.getChildAt(i);
            if (child instanceof TextView &&
                    ((TextView)child).getText().toString().startsWith(blockName + ":")) {
                return (TextView) child;
            }
        }

        // 创建新的区块视图
        TextView newBlockView = new TextView(this);
        newBlockView.setPadding(16, 8, 16, 8);
        newBlockView.setTextSize(14);
        blockResultsContainer.addView(newBlockView);

        // 滚动到底部以显示最新的区块结果
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        return newBlockView;
    }

    @SuppressLint("DefaultLocale")
    private void updateFinalStatus() {
        if (textView == null) return;

        double percentage = numTotal > 0 ? ((double) numValid / numTotal) * 100 : 0;
        String grade = Grade.fromScore(percentage);

        textView.setText(grade);
        textView5.setVisibility(View.VISIBLE);
        textView5.setText(String.format("%.3f%%", percentage));

        // 添加总体结果
        TextView overallResultView = new TextView(this);
        overallResultView.setPadding(16, 16, 16, 16);
        overallResultView.setTextSize(16);
        overallResultView.setText(String.format("总体结果: %s - %d/%d (%.2f%%)",
                grade, numValid, numTotal, percentage));
        overallResultView.setBackgroundColor(0xFF808080);
        blockResultsContainer.addView(overallResultView, 0);
    }

    private String formatUnicode(String line) {
        int spaceIndex = line.indexOf(' ');
        String firstField = spaceIndex > 0 ? line.substring(0, spaceIndex) : line;
        return firstField.replace("U+", "&#x");
    }

    @Override
    protected void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }

    // 区块统计类
    private static class BlockStatistics {
        int valid = 0;
        int total = 0;
    }

    public enum Grade {
        PG(100.0, "PG"),   // Pure Glyphs
        EX(96.0, "EX"),    // Excellent
        A(90.0, "A"),      // Amazing
        B(82.0, "B"),      // Better
        C(70.0, "C"),      // Clear
        D(62.0, "D"),      // Default
        E(42.0, "E"),      // Easy
        F(0.0, "F");       // Failed

        private final double threshold;
        private final String symbol;

        Grade(double threshold, String symbol) {
            this.threshold = threshold;
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public static String fromScore(double score) {
            if (Math.abs(score - 100.0) < EPSILON) {
                return PG.symbol;
            }
            for (Grade grade : values()) {
                if (score >= grade.threshold) {
                    return grade.symbol;
                }
            }
            return F.symbol;
        }
    }
}