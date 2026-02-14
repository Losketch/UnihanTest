package top.rainysummer.unihantest;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnicodeDataCache {
    private static final Set<String> FILTER_CATEGORIES = new HashSet<>(java.util.Arrays.asList(
            "Cc", "Cf", "Co", "Zs", "Zl", "Zp", "Mn", "Cs", "Cn"
    ));

    private static volatile List<String> cachedUnicodeLines = null;
    private static volatile List<String> cachedHanScriptsLines = null;

    public static void clearCache() {
        cachedUnicodeLines = null;
        cachedHanScriptsLines = null;
    }

    public static List<String> getUnicodeDataLines(Context context, AssetManager assetManager) throws IOException {
        if (cachedUnicodeLines != null) {
            return cachedUnicodeLines;
        }

        synchronized (UnicodeDataCache.class) {
            if (cachedUnicodeLines != null) {
                return cachedUnicodeLines;
            }

            List<CodeBlock> blocks = loadBlocks(assetManager, "Blocks.txt");
            List<Integer> codepoints = parseUnicodeData(assetManager, "UnicodeData.txt");
            cachedUnicodeLines = generateLinesWithBlocks(codepoints, blocks);
            return cachedUnicodeLines;
        }
    }

    public static List<String> getHanScriptsLines(Context context, AssetManager assetManager) throws IOException {
        if (cachedHanScriptsLines != null) {
            return cachedHanScriptsLines;
        }

        synchronized (UnicodeDataCache.class) {
            if (cachedHanScriptsLines != null) {
                return cachedHanScriptsLines;
            }

            List<CodeBlock> blocks = loadBlocks(assetManager, "Blocks.txt");
            List<Integer> codepoints = parseHanScripts(assetManager, "Scripts.txt", "ScriptExtensions.txt");
            cachedHanScriptsLines = generateLinesWithBlocks(codepoints, blocks);
            return cachedHanScriptsLines;
        }
    }

    private static List<CodeBlock> loadBlocks(AssetManager assetManager, String blocksFile) throws IOException {
        List<CodeBlock> blocks = new ArrayList<>();

        try (InputStream is = assetManager.open(blocksFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String commentRemoved = line.split("#", 2)[0].trim();
                if (commentRemoved.isEmpty()) continue;

                String[] parts = commentRemoved.split(";", 2);
                if (parts.length < 2) continue;

                String rangeStr = parts[0].trim();
                if (rangeStr.isEmpty()) continue;

                String name = parts[1].trim();

                try {
                    CodeBlock block = parseCodeBlock(rangeStr, name);
                    if (block != null) {
                        blocks.add(block);
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        Collections.sort(blocks);
        return blocks;
    }

    private static CodeBlock parseCodeBlock(String rangeStr, String name) {
        int start, end;
        if (rangeStr.contains("..")) {
            String[] parts = rangeStr.split("\\.\\.");
            if (parts.length < 2) return null;
            start = Integer.parseInt(parts[0], 16);
            end = Integer.parseInt(parts[1], 16);
        } else {
            start = end = Integer.parseInt(rangeStr, 16);
        }
        return new CodeBlock(start, end, name);
    }

    private static List<Integer> parseUnicodeData(AssetManager assetManager, String unicodeDataFile) throws IOException {
        List<Integer> codepoints = new ArrayList<>();
        Integer startCp = null;

        try (InputStream is = assetManager.open(unicodeDataFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                if (fields.length < 3) continue;

                String cpHex = fields[0].trim();
                String name = fields[1].trim();
                String category = fields[2].trim();

                if (FILTER_CATEGORIES.contains(category)) continue;

                int cp;
                try {
                    cp = Integer.parseInt(cpHex, 16);
                } catch (NumberFormatException e) {
                    continue;
                }

                if (name.startsWith("<") && name.endsWith("First>")) {
                    startCp = cp;
                } else if (name.startsWith("<") && name.endsWith("Last>") && startCp != null) {
                    for (int i = startCp; i <= cp; i++) {
                        codepoints.add(i);
                    }
                    startCp = null;
                } else {
                    codepoints.add(cp);
                }
            }
        }

        return deduplicateAndSort(codepoints);
    }

    private static List<Integer> parseHanScripts(AssetManager assetManager, String scriptsFile, String scriptExtensionsFile) throws IOException {
        List<int[]> ranges = new ArrayList<>();

        if (scriptsFile != null) {
            ranges.addAll(parseScriptFile(assetManager, scriptsFile, "Han"));
        }
        if (scriptExtensionsFile != null) {
            ranges.addAll(parseScriptExtensionsFile(assetManager, scriptExtensionsFile, "Hani"));
        }

        List<int[]> mergedRanges = mergeRanges(ranges);
        List<Integer> codepoints = new ArrayList<>();
        for (int[] range : mergedRanges) {
            for (int cp = range[0]; cp <= range[1]; cp++) {
                codepoints.add(cp);
            }
        }

        return deduplicateAndSort(codepoints);
    }

    private static List<int[]> parseScriptFile(AssetManager assetManager, String scriptsFile, String targetScript) throws IOException {
        List<int[]> ranges = new ArrayList<>();

        try (InputStream is = assetManager.open(scriptsFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String commentRemoved = line.split("#", 2)[0].trim();
                if (commentRemoved.isEmpty()) continue;

                String[] parts = commentRemoved.split(";", 2);
                if (parts.length < 2) continue;

                String rangeStr = parts[0].trim();
                if (rangeStr.isEmpty()) continue;

                String script = parts[1].trim();

                if (targetScript.equals(script)) {
                    try {
                        int[] range = parseCodeRange(rangeStr);
                        if (range != null) {
                            ranges.add(range);
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }

        return ranges;
    }

    private static List<int[]> parseScriptExtensionsFile(AssetManager assetManager, String scriptExtensionsFile, String targetScript) throws IOException {
        List<int[]> ranges = new ArrayList<>();

        try (InputStream is = assetManager.open(scriptExtensionsFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String commentRemoved = line.split("#", 2)[0].trim();
                if (commentRemoved.isEmpty()) continue;

                String[] parts = commentRemoved.split(";", 2);
                if (parts.length < 2) continue;

                String rangeStr = parts[0].trim();
                if (rangeStr.isEmpty()) continue;

                String scripts = parts[1].trim();

                if (scripts.contains(targetScript)) {
                    try {
                        int[] range = parseCodeRange(rangeStr);
                        if (range != null) {
                            ranges.add(range);
                        }
                    } catch (NumberFormatException e) {
                        continue;
                    }
                }
            }
        }

        return ranges;
    }

    private static int[] parseCodeRange(String rangeStr) {
        if (rangeStr.contains("..")) {
            String[] parts = rangeStr.split("\\.\\.");
            if (parts.length < 2) return null;
            return new int[]{
                    Integer.parseInt(parts[0], 16),
                    Integer.parseInt(parts[1], 16)
            };
        } else {
            int code = Integer.parseInt(rangeStr, 16);
            return new int[]{code, code};
        }
    }

    private static List<int[]> mergeRanges(List<int[]> ranges) {
        if (ranges.isEmpty()) return new ArrayList<>();

        Collections.sort(ranges, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> merged = new ArrayList<>();
        merged.add(ranges.get(0));

        for (int i = 1; i < ranges.size(); i++) {
            int[] current = ranges.get(i);
            int[] last = merged.get(merged.size() - 1);

            if (current[0] <= last[1] + 1) {
                last[1] = Math.max(last[1], current[1]);
            } else {
                merged.add(current);
            }
        }

        return merged;
    }

    private static List<Integer> deduplicateAndSort(List<Integer> list) {
        Set<Integer> set = new HashSet<>(list);
        List<Integer> result = new ArrayList<>(set);
        Collections.sort(result);
        return result;
    }

    private static List<String> generateLinesWithBlocks(List<Integer> codepoints, List<CodeBlock> blocks) {
        List<String> lines = new ArrayList<>();
        String lastBlockName = null;

        for (int cp : codepoints) {
            CodeBlock block = findBlock(blocks, cp);
            if (block == null) continue;

            if (!block.name.equals(lastBlockName)) {
                lines.add(String.format("#BLOCK:%04X; %s", block.start, block.name));
                lastBlockName = block.name;
            }

            lines.add(String.format("U+%04X", cp));
        }

        return lines;
    }

    private static CodeBlock findBlock(List<CodeBlock> blocks, int codepoint) {
        for (CodeBlock block : blocks) {
            if (codepoint >= block.start && codepoint <= block.end) {
                return block;
            }
        }
        return null;
    }

    private static class CodeBlock implements Comparable<CodeBlock> {
        final int start;
        final int end;
        final String name;

        CodeBlock(int start, int end, String name) {
            this.start = start;
            this.end = end;
            this.name = name;
        }

        @Override
        public int compareTo(CodeBlock other) {
            return Integer.compare(this.start, other.start);
        }
    }
}
