package top.rainysummer.unihantest;

public class UnihanTestActivity extends BaseUnicodeTestActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_unihan_test;
    }

    @Override
    protected String getAssetFileName() {
        return "Unihan.txt";
    }

    @Override
    protected FileMode getFileMode() {
        return FileMode.RAW_HAN_SCRIPTS;
    }

    @Override
    protected String getBlocksFileName() {
        return "Blocks.txt";
    }

    @Override
    protected String getSecondaryFileName() {
        return "Scripts.txt";
    }

    @Override
    protected String getTertiaryFileName() {
        return "ScriptExtensions.txt";
    }
}
