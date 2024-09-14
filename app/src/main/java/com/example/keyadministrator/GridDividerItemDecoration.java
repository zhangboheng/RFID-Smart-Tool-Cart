package com.example.keyadministrator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

    private final int dividerWidth;
    private final Paint dividerPaint;

    public GridDividerItemDecoration(Context context, int dividerWidth, int dividerColor){
        this.dividerWidth = dividerWidth;
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
                int dividerLeft = child.getRight();
                int dividerTop = child.getTop();
                int dividerRight = dividerLeft + dividerWidth;
                int dividerBottom = child.getHeight() * 3;
                c.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, dividerPaint);
            }
        }
    }
}
