package top.rainysummer.unihantest;

public class FontDetector {
    static {
        System.loadLibrary("font_detector");
    }

    public static native void nativeInit();

    public static native boolean nativeHasGlyph(int codepoint);

    private static volatile boolean initialized = false;

    public static void init() {
        if (!initialized) {
            synchronized (FontDetector.class) {
                if (!initialized) {
                    nativeInit();
                    initialized = true;
                }
            }
        }
    }

    public static boolean hasGlyph(int codepoint) {
        init();
        return nativeHasGlyph(codepoint);
    }

    public static boolean hasGlyph(char c) {
        return hasGlyph((int) c);
    }
}
