package top.rainysummer.unihantest;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UnicodeFileProcessor {
    private static final int BATCH_SIZE = 80;
    private static final String BLOCK_PREFIX = "#BLOCK:";

    private final AssetManager assetManager;
    private final String fileName;
    private final ProcessCallback callback;
    private final Context context;

    public interface ProcessCallback {
        void onProgress(String formattedUnicode, int validCount, int totalCount);
        void onBatchComplete(String batchText);
        void onBlockStart(String blockName);
        void onComplete(int totalLines);
        void onError(String error);
        void onLineCountUpdate(int lineCount);
    }

    public UnicodeFileProcessor(Context context, AssetManager assetManager, String fileName, ProcessCallback callback) {
        this.context = context.getApplicationContext();
        this.assetManager = assetManager;
        this.fileName = fileName;
        this.callback = callback;
    }

    public void process() {
        try {
            int lineCount = countValidLines();
            callback.onLineCountUpdate(lineCount);
            processFile();
        } catch (IOException e) {
            String errMsg = context.getString(R.string.error_read_file_fail, fileName, e.getMessage());
            callback.onError(errMsg);
        }
    }

    private int countValidLines() throws IOException {
        int lineCount = 0;
        try (InputStreamReader inputReader = new InputStreamReader(assetManager.open(fileName));
             BufferedReader bufferedReader = new BufferedReader(inputReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                lineCount++;
            }
        }
        return lineCount;
    }

    private void processFile() throws IOException {
        try (InputStreamReader inputReader = new InputStreamReader(assetManager.open(fileName));
             BufferedReader bufferedReader = new BufferedReader(inputReader)) {

            String line;
            int batchCount = 0;
            int validCount = 0;
            int totalCount = 0;
            StringBuilder batchBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith(BLOCK_PREFIX)) {
                    String blockName = line.substring(BLOCK_PREFIX.length()).trim();
                    callback.onBlockStart(blockName);
                    continue;
                }

                if (line.startsWith("#")) {
                    continue;
                }

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
}
