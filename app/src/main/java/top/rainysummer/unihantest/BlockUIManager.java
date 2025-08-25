package top.rainysummer.unihantest;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class BlockUIManager {

    public static TextView findOrCreateBlockView(Context context, LinearLayout container,
                                                 ScrollView scrollView, String blockName) {
        // 查找是否已有该区块的视图
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView &&
                    ((TextView)child).getText().toString().startsWith(blockName + ":")) {
                return (TextView) child;
            }
        }

        // 创建新的区块视图
        TextView newBlockView = new TextView(context);
        newBlockView.setPadding(16, 8, 16, 8);
        newBlockView.setTextSize(14);
        container.addView(newBlockView);

        // 滚动到底部以显示最新的区块结果
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

        return newBlockView;
    }

    public static TextView createOverallResultView(Context context, String grade,
                                                   int numValid, int numTotal, double percentage) {
        TextView overallResultView = new TextView(context);
        overallResultView.setPadding(16, 16, 16, 16);
        overallResultView.setTextSize(16);
        overallResultView.setText(context.getString(R.string.overall_result_format,
                grade, numValid, numTotal, percentage));
        overallResultView.setBackgroundColor(0xFF808080);
        return overallResultView;
    }
}
