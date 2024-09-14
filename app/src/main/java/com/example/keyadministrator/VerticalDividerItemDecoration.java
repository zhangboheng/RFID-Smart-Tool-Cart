package com.example.keyadministrator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final int dividerHeight;
    private final Paint dividerPaint;

    public VerticalDividerItemDecoration(Context context, int dividerHeight, int dividerColor) {
        this.dividerHeight =dividerHeight;
        dividerPaint = new Paint();
        dividerPaint.setColor(dividerColor);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);// 绘制竖线
            if ((position + 1) % spanCount != 0) {
                int dividerTop = child.getTop();
                int dividerLeft = child.getRight();
                int dividerRight = dividerLeft + dividerHeight;
                int dividerBottom = child.getBottom() + child.getHeight();
                c.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, dividerPaint);
            }
        }
    }
}
