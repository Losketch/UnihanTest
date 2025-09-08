package top.rainysummer.unihantest;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseUnicodeTestActivity extends AppCompatActivity {

    private static final double EPSILON = 0.0001;

    private final AtomicInteger pendingBatchCounter = new AtomicInteger(0);
    private volatile long lastUiUpdateMillis = 0;
    private static final int UI_UPDATE_THROTTLE_MS = 80; // 每 ms 至多一次 UI 更新
    private static final int UI_BATCH_THRESHOLD = 5; // 每处理 5 批 或达到时间间隔则更新 UI

    // 使用原子类型确保线程安全
    private final AtomicInteger totalValidCount = new AtomicInteger(0);
    private final AtomicInteger totalProcessedCount = new AtomicInteger(0);

    private final Map<String, BlockStatistics> blockStats = new HashMap<>();
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
        // 在工作线程中执行，传入复用的 paint 到处理器回调里以减少重复创建
        final Paint workerPaint = paint == null ? new Paint() : paint;
        UnicodeFileProcessor processor = new UnicodeFileProcessor(
                getApplicationContext(),
                getResources().getAssets(),
                getAssetFileName(),
                new UnicodeFileProcessor.ProcessCallback() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgress(String formattedUnicode, int validCount, int totalCount) {
                        // 后台只更新 block 统计和局部计数，不频繁 post UI
                        BlockStatistics stats = blockStats.get(currentBlock);
                        if (stats != null) {
                            stats.total++;
                            if (UnicodeValidator.isValidEmoji(workerPaint, formattedUnicode)) {
                                stats.valid++;
                            }
                        }

                        // 仅用于显示进度的计数（不做最终计算）
                        totalProcessedCount.incrementAndGet();
                        if (validCount > 0) {
                            totalValidCount.set(validCount); // 保持与处理器的进度一致（仅用于临时显示）
                        }

                        // 节流 UI 更新：按批次数或时间间隔更新
                        int pending = pendingBatchCounter.incrementAndGet();
                        long now = System.currentTimeMillis();
                        if (pending >= UI_BATCH_THRESHOLD || now - lastUiUpdateMillis >= UI_UPDATE_THROTTLE_MS) {
                            pendingBatchCounter.set(0);
                            lastUiUpdateMillis = now;
                            mainHandler.post(() -> {
                                textView2.setText(getString(R.string.progress_format, validCount, totalCount) + " = ");
                                textView4.setText(totalCount + " / " + progressBar.getMax());
                                progressBar.setProgress(totalCount);
                            });
                        }
                    }

                    @Override
                    public void onBatchComplete(String batchText) {
                        // 按批更新 UI（但同样节流），直接调用 postUpdateUI 会再由 mainHandler 执行
                        postUpdateUI(batchText);
                    }

                    @Override
                    public void onBlockStart(String blockName) {
                        if (blockStats.containsKey(currentBlock)) {
                            postBlockUpdate(currentBlock);
                        }
                        currentBlock = blockName;
                        CompatUtils.mapPutIfAbsent(blockStats, currentBlock, new BlockStatistics());
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(int totalLines) {
                        // 处理最后一个区块
                        postBlockUpdate(currentBlock);

                        // 计算总体统计（从后台收集的 blockStats）
                        calculateOverallStatistics();

                        mainHandler.post(() -> {
                            progressBar.setMax(totalLines);
                            progressBar.setProgress(totalLines);
                            textView3.setVisibility(View.GONE);
                            textView4.setText(progressBar.getMax() + " / " + progressBar.getMax());
                            updateFinalStatus();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        postError(getString(R.string.processing_error, error));
                    }

                    @Override
                    public void onLineCountUpdate(int lineCount) {
                        mainHandler.post(() -> progressBar.setMax(lineCount));
                    }
                }
        );

        // 在后台线程执行处理（已经在 executor 中）
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
        if (batchLines == null || batchLines.isEmpty()) return;

        // 只显示最后一行作为预览，避免反复解析整个批次
        String[] lines = batchLines.split("\n");
        String lastLine = lines[lines.length - 1];

        // 仅把最必要的 UI 操作放到主线程
        textView.setText(CompatUtils.fromHtml(lastLine));
        // textView2 和 progress 的更新由节流逻辑控制并可能已更新，这里可安全覆盖一次即时值
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

        blockView.setText(getString(R.string.block_result_format,
                blockName, grade, stats.valid, stats.total, percentage));
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void updateFinalStatus() {
        if (textView == null) return;

        // 使用计算出的总体统计数据
        int finalValid = totalValidCount.get();
        int finalTotal = totalProcessedCount.get();

        double percentage = finalTotal > 0 ? ((double) finalValid / finalTotal) * 100 : 0;
        String grade = Grade.fromScore(percentage);

        textView.setText(grade);
        textView5.setVisibility(View.VISIBLE);
        textView5.setText(getString(R.string.percentage_format, percentage));

        // 更新最终的统计显示
        textView2.setText(getString(R.string.progress_format, finalValid, finalTotal) + " = ");

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
