package top.rainysummer.unihantest;

public class UnicodeTestActivity extends BaseUnicodeTestActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_unicode_test;
    }

    @Override
    protected String getAssetFileName() {
        return "Unicode.txt";
    }

    @Override
    protected FileMode getFileMode() {
        return FileMode.RAW_UNICODE_DATA;
    }

    @Override
    protected String getBlocksFileName() {
        return "Blocks.txt";
    }

    @Override
    protected String getSecondaryFileName() {
        return "UnicodeData.txt";
    }
}
