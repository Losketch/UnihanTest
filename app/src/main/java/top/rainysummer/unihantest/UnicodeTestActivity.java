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
}