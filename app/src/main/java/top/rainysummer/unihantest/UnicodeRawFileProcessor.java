package top.rainysummer.unihantest;

import android.content.Context;
import android.content.res.AssetManager;

import java.util.List;

public class UnicodeRawFileProcessor {
    private static final int BATCH_SIZE = 80;
    private static final String BLOCK_PREFIX = "#BLOCK:";

    private final AssetManager assetManager;
    private final Context context;
    private final UnicodeFileProcessor.ProcessCallback callback;

    public UnicodeRawFileProcessor(Context context, AssetManager assetManager, UnicodeFileProcessor.ProcessCallback callback) {
        this.context = context.getApplicationContext();
        this.assetManager = assetManager;
        this.callback = callback;
    }

    public void processFromUnicodeDataLines(List<String> lines) {
        processLines(lines);
    }

    public void processFromHanScriptsLines(List<String> lines) {
        processLines(lines);
    }

    private void processLines(List<String> lines) {
        int batchCount = 0;
        int validCount = 0;
        int totalCount = 0;
        StringBuilder batchBuilder = new StringBuilder();

        for (String line : lines) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith(BLOCK_PREFIX)) {
                String blockName = line.substring(BLOCK_PREFIX.length()).trim();
                if (blockName.contains(";")) {
                    blockName = blockName.substring(blockName.indexOf(";") + 1).trim();
                }
                callback.onBlockStart(blockName);
                continue;
            }

            if (line.startsWith("#")) continue;

            totalCount++;
            String formattedUnicode = UnicodeFormatter.formatUnicode(line);

            if (UnicodeValidator.isValidEmoji(null, formattedUnicode)) {
                validCount++;
            }

            callback.onProgress(formattedUnicode, validCount, totalCount);

            batchBuilder.append(formattedUnicode).append("\n");
            batchCount++;

            if (batchCount % BATCH_SIZE == 0) {
                callback.onBatchComplete(batchBuilder.toString());
                batchBuilder.setLength(0);
            }
        }

        if (batchBuilder.length() > 0) {
            callback.onBatchComplete(batchBuilder.toString());
        }

        callback.onComplete(totalCount);
    }
}
