package top.rainysummer.unihantest;

import android.app.Application;
import android.content.res.AssetManager;

public class App extends Application {
    private static App instance;
    private AssetManager assetManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        assetManager = getAssets();
        preLoadCache();
        new Thread(() -> {
            int retryCount = 0;
            int maxRetries = 3;
            while (retryCount < maxRetries) {
                try {
                    FontDetector.init();
                    break;
                } catch (Exception e) {
                    retryCount++;
                    e.printStackTrace();
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(1000 * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    public static App getInstance() {
        return instance;
    }

    public AssetManager getAppAssetManager() {
        return assetManager;
    }

    private void preLoadCache() {
        new Thread(() -> {
            try {
                UnicodeDataCache.getUnicodeDataLines(instance, assetManager);
                UnicodeDataCache.getHanScriptsLines(instance, assetManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
