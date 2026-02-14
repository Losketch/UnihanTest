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
