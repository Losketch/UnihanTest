// UnihanTestActivity.java (重构后)
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
}