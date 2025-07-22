import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class UnicodeRange {
    private String name;
    private List<Object> codes;
    
    public UnicodeRange(String name) {
        this.name = name;
        this.codes = new ArrayList<>();
    }
    
    public UnicodeRange addSingleCode(int code) {
        codes.add(code);
        return this;
    }
    
    public UnicodeRange addRange(int start, int end) {
        codes.add(new int[]{start, end});
        return this;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Object> getCodes() {
        return codes;
    }
}

public class GenerateUnihanTXT {
    
    public static int writeUnicodeRanges(String filename, List<UnicodeRange> ranges, boolean addComments) throws IOException {
        /* Write Unicode ranges to a file with proper formatting and comments. */
        int count = 0;
        try (FileWriter writer = new FileWriter(filename, StandardCharsets.UTF_8)) {
            for (UnicodeRange rangeGroup : ranges) {
                if (addComments && rangeGroup.getName() != null && !rangeGroup.getName().isEmpty()) {
                    writer.write("#BLOCK:" + rangeGroup.getName() + "\n");
                }
                
                for (Object code : rangeGroup.getCodes()) {
                    if (code instanceof int[]) {
                        int[] range = (int[]) code;
                        int start = range[0];
                        int end = range[1];
                        for (int codePoint = start; codePoint <= end; codePoint++) {
                            writer.write(String.format("U+%04X\n", codePoint));
                            count++;
                        }
                    } else if (code instanceof Integer) {
                        writer.write(String.format("U+%04X\n", (Integer) code));
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    // Define Unicode ranges with meaningful names
    public static void main(String[] args) {
        List<UnicodeRange> unicodeRanges = new ArrayList<>();
        
        unicodeRanges.add(new UnicodeRange("2F00; 康熙部首").addRange(0x2F00, 0x2FD5));
        unicodeRanges.add(new UnicodeRange("2FF0; 表意文字描述字符")
            .addRange(0x2FF0, 0x2FFF)
            .addSingleCode(0x31EF));
        unicodeRanges.add(new UnicodeRange("2E80; 中日韩部首补充")
            .addRange(0x2E80, 0x2E99)
            .addRange(0x2E9B, 0x2EF3));
        
        unicodeRanges.add(new UnicodeRange("3007; 〇").addSingleCode(0x3007));
        unicodeRanges.add(new UnicodeRange("31C0; 中日韩汉字笔画").addRange(0x31C0, 0x31E5));
        
        unicodeRanges.add(new UnicodeRange("4E00; 基本区").addRange(0x4E00, 0x9FFF));
        unicodeRanges.add(new UnicodeRange("3400; 扩展A").addRange(0x3400, 0x4DBF));
        unicodeRanges.add(new UnicodeRange("F900; 兼容区")
            .addRange(0xF900, 0xFA6D)
            .addRange(0xFA70, 0xFAD9));
        
        unicodeRanges.add(new UnicodeRange("20000; 扩展B").addRange(0x20000, 0x2A6DF));
        unicodeRanges.add(new UnicodeRange("2A700; 扩展C").addRange(0x2A700, 0x2B73F));
        unicodeRanges.add(new UnicodeRange("2B740; 扩展D").addRange(0x2B740, 0x2B81D));
        unicodeRanges.add(new UnicodeRange("2B820; 扩展E").addRange(0x2B820, 0x2CEAD));
        unicodeRanges.add(new UnicodeRange("2CEB0; 扩展F").addRange(0x2CEB0, 0x2EBE0));
        unicodeRanges.add(new UnicodeRange("2F800; 兼容补充区").addRange(0x2F800, 0x2FA1D));
        
        unicodeRanges.add(new UnicodeRange("30000; 扩展G").addRange(0x30000, 0x3134A));
        unicodeRanges.add(new UnicodeRange("31350; 扩展H").addRange(0x31350, 0x323AF));
        unicodeRanges.add(new UnicodeRange("2EBF0; 扩展I").addRange(0x2EBF0, 0x2EE5D));
        unicodeRanges.add(new UnicodeRange("323B0; 扩展J").addRange(0x323B0, 0x33479));
        
        boolean addComments = true;  // Whether to add comment names (True/False)
        String filePath = System.getProperty("user.dir") + "/Unihan.txt";
        
        try {
            int count = writeUnicodeRanges(filePath, unicodeRanges, addComments);
            System.out.println("Total " + count + " rows of UniHan characters");
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
