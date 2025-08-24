package top.rainysummer.unihantest;

import android.graphics.Paint;
import android.text.Spanned;

public class UnicodeValidator {
    private static final String SPECIAL_UNICODE = "&#x1F1E8&#x1F1F3";
    private static final String ZWJ_SEQUENCE = "&#x200D&#x";

    public static boolean isValidEmoji(Paint paint, String unicodeHtml) {
        if (SPECIAL_UNICODE.equals(unicodeHtml)) {
            return true;
        }

        Spanned sp = CompatUtils.fromHtml(unicodeHtml);
        String parsed = sp.toString();

        if (paint != null && !CompatUtils.hasGlyph(paint, parsed)) {
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
}
