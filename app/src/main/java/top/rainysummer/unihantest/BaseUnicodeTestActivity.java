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
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseUnicodeTestActivity extends AppCompatActivity {

    private static final double EPSILON = 0.0001;
    private static final String SPECIAL_UNICODE = "&#x1F1E8&#x1F1F3";
    private static final String ZWJ_SEQUENCE = "&#x200D&#x";
    private static final int BATCH_SIZE = 80;
    private static final String BLOCK_PREFIX = "#BLOCK:";

    // 使用原子类型确保线程安全
    private final AtomicInteger totalValidCount = new AtomicInteger(0);
    private final AtomicInteger totalProcessedCount = new AtomicInteger(0);

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
        setContentView(getLayoutId());
        initViews();

        paint = new Paint();
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newSingleThreadExecutor();

        progressBar.setMax(1000);
        executorService.execute(this::processUnicodeFile);
    }

    // 抽象方法，由子类实现
    protected abstract int getLayoutId();
    protected abstract String getAssetFileName();

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
        UnicodeFileProcessor processor = new UnicodeFileProcessor(
                getResources().getAssets(),
                getAssetFileName(),
                new UnicodeFileProcessor.ProcessCallback() {
                    @Override
                    public void onProgress(String formattedUnicode, int validCount, int totalCount) {
                        // 更新当前区块统计
                        BlockStatistics stats = blockStats.get(currentBlock);
                        if (stats != null) {
                            stats.total++;
                            if (UnicodeValidator.isValidEmoji(paint, formattedUnicode)) {
                                stats.valid++;
                            }
                        }

                        // 更新总体进度显示（但不用于最终计算）
                        mainHandler.post(() -> {
                            textView2.setText(validCount + " / " + totalCount + " = ");
                            textView4.setText(totalCount + " / " + progressBar.getMax());
                            progressBar.setProgress(totalCount);
                        });
                    }

                    @Override
                    public void onBatchComplete(String batchText) {
                        postUpdateUI(batchText);
                    }

                    @Override
                    public void onBlockStart(String blockName) {
                        // 完成上一个区块的处理
                        if (blockStats.containsKey(currentBlock)) {
                            postBlockUpdate(currentBlock);
                        }

                        // 开始新区块
                        currentBlock = blockName;
                        CompatUtils.mapPutIfAbsent(blockStats, currentBlock, new BlockStatistics());
                    }

                    @Override
                    public void onComplete(int totalLines) {
                        // 处理最后一个区块
                        postBlockUpdate(currentBlock);

                        // 计算总体统计
                        calculateOverallStatistics();

                        mainHandler.post(() -> {
                            progressBar.setMax(totalLines);
                            progressBar.setProgress(totalLines);
                            textView3.setVisibility(View.GONE);
                            updateFinalStatus();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        postError(error);
                    }

                    @Override
                    public void onLineCountUpdate(int lineCount) {
                        mainHandler.post(() -> progressBar.setMax(lineCount));
                    }
                }
        );

        processor.process();
    }

    // 计算所有区块的总体统计
    private void calculateOverallStatistics() {
        int totalValid = 0;
        int totalProcessed = 0;

        for (BlockStatistics stats : blockStats.values()) {
            totalValid += stats.valid;
            totalProcessed += stats.total;
        }

        totalValidCount.set(totalValid);
        totalProcessedCount.set(totalProcessed);
    }

    private void postBlockUpdate(String blockName) {
        mainHandler.post(() -> updateBlockUI(blockName));
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

    @SuppressLint("SetTextI18n")
    private void updateUI(String batchLines) {
        if (batchLines.isEmpty()) return;

        String[] lines = batchLines.split("\n");
        String lastLine = lines[lines.length - 1];

        textView.setText(CompatUtils.fromHtml(lastLine));
        textView3.setText(lastLine.replace("&#x", " "));
        textView5.setVisibility(View.GONE);
    }

    @SuppressLint("DefaultLocale")
    private void updateBlockUI(String blockName) {
        BlockStatistics stats = blockStats.get(blockName);
        if (stats == null) return;

        TextView blockView = BlockUIManager.findOrCreateBlockView(
                this, blockResultsContainer, scrollView, blockName);
        double percentage = stats.total > 0 ? ((double) stats.valid / stats.total) * 100 : 0;
        String grade = Grade.fromScore(percentage);

        blockView.setText(String.format("%s:\n %s - %d/%d (%.2f%%)",
                blockName, grade, stats.valid, stats.total, percentage));
    }

    @SuppressLint("DefaultLocale")
    private void updateFinalStatus() {
        if (textView == null) return;

        // 使用计算出的总体统计数据
        int finalValid = totalValidCount.get();
        int finalTotal = totalProcessedCount.get();

        double percentage = finalTotal > 0 ? ((double) finalValid / finalTotal) * 100 : 0;
        String grade = Grade.fromScore(percentage);

        textView.setText(grade);
        textView5.setVisibility(View.VISIBLE);
        textView5.setText(String.format("%.3f%%", percentage));

        // 更新最终的统计显示
        textView2.setText(finalValid + " / " + finalTotal + " = ");

        TextView overallResultView = BlockUIManager.createOverallResultView(
                this, grade, finalValid, finalTotal, percentage);
        blockResultsContainer.addView(overallResultView, 0);
    }

    @Override
    protected void onDestroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }

    // 区块统计类
    protected static class BlockStatistics {
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
