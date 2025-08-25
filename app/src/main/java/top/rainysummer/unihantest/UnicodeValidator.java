package top.rainysummer.unihantest;

import android.graphics.Paint;
import android.text.Spanned;
import android.os.Build;

public class UnicodeValidator {
    private static final String SPECIAL_UNICODE = "&#x1F1E8&#x1F1F3";
    private static final String ZWJ_SEQUENCE = "&#x200D&#x";

    public static boolean isValidEmoji(Paint paint, String unicodeHtml) {
        if (SPECIAL_UNICODE.equals(unicodeHtml)) return true;
        try {
            // 快速排除：ZWJ 情况先用简单比较（如果字符串形式长度不同则可能是组合）
            boolean containsZwj = unicodeHtml.contains(ZWJ_SEQUENCE);
            Spanned sp = CompatUtils.fromHtml(unicodeHtml);
            String parsed = sp.toString();

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (paint != null && !CompatUtils.hasGlyph(paint, parsed)) {
                    return false;
                }
            } else {
                if (SPECIAL_UNICODE.equals(unicodeHtml)) return true;
            }

            if (containsZwj) {
                String replaced = unicodeHtml.replace(ZWJ_SEQUENCE, "&#x");
                Spanned sp2 = CompatUtils.fromHtml(replaced);
                String replacedStr = sp2.toString();
                return parsed.length() != replacedStr.length();
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
