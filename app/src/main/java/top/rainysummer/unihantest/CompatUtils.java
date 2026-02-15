package top.rainysummer.unihantest;

import android.graphics.Paint;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import java.util.Map;

public class CompatUtils {

    /**
     * 兼容各 API 的 Html.fromHtml 调用
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API 24+ 可用重载
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            // 低版本使用老接口
            return Html.fromHtml(source);
        }
    }

    /**
     * Rust 实现的 hasGlyph 检测
     */
    public static boolean hasGlyph(Paint paint, String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isHighSurrogate(c) && i + 1 < text.length()) {
                char low = text.charAt(i + 1);
                if (Character.isLowSurrogate(low)) {
                    int codepoint = Character.toCodePoint(c, low);
                    if (!FontDetector.hasGlyph(codepoint)) {
                        return false;
                    }
                    i++;
                }
            } else {
                if (!FontDetector.hasGlyph(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 兼容各 API 的 Map.putIfAbsent 调用
     */
    public static <K, V> void mapPutIfAbsent(Map<K, V> map, K key, V value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // API24+ 直接调用原生方法
            map.putIfAbsent(key, value);
        } else {
            // API<24 手动实现
            if (!map.containsKey(key)) {
                map.put(key, value);
            }
        }
    }
}