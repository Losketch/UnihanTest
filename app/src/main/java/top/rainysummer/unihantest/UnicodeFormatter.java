package top.rainysummer.unihantest;

public class UnicodeFormatter {
    public static String formatUnicode(String line) {
        int spaceIndex = line.indexOf(' ');
        String firstField = spaceIndex > 0 ? line.substring(0, spaceIndex) : line;
        return firstField.replace("U+", "&#x");
    }
}
