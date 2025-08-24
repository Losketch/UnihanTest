package top.rainysummer.unihantest;

import android.graphics.Paint;
import android.text.Spanned;
import android.os.Build;

public class UnicodeValidator {
    private static final String SPECIAL_UNICODE = "&#x1F1E8&#x1F1F3";
    private static final String ZWJ_SEQUENCE = "&#x200D&#x";

    public static boolean isValidEmoji(Paint paint, String unicodeHtml) {
        if (SPECIAL_UNICODE.equals(unicodeHtml)) {
            return true;
        }

        try {
            Spanned sp = CompatUtils.fromHtml(unicodeHtml);
            String parsed = sp.toString();

            // 在 Android 5.1.1 上，hasGlyph 方法可能不可靠
            if (paint != null && Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (!CompatUtils.hasGlyph(paint, parsed)) {
                    return false;
                }
            }

            if (unicodeHtml.contains(ZWJ_SEQUENCE)) {
                String replaced = unicodeHtml.replace(ZWJ_SEQUENCE, "&#x");
                Spanned sp2 = CompatUtils.fromHtml(replaced);
                String replacedStr = sp2.toString();
                return parsed.length() != replacedStr.length();
            }

            return true;
        } catch (Exception e) {
            // 解析失败时返回 false
            return false;
        }
    }
}
