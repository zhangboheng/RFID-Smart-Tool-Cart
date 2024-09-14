package com.example.keyadministrator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoodsTotalAdapter  extends RecyclerView.Adapter<GoodsTotalAdapter.GoodViewHolder>{
    private List<Good> goodsList;

    public GoodsTotalAdapter(List<Good> goodsList) {
        this.goodsList = goodsList;
    }

    @NonNull
    @Override
    public GoodsTotalAdapter.GoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_good_total, parent, false);
        return new GoodsTotalAdapter.GoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GoodsTotalAdapter.GoodViewHolder holder, int position) {
        Good good = goodsList.get(position);
        holder.textViewItemName.setText(good.getTypeName());
        holder.textViewCabinetNumber.setText(good.getBelongingCabinetNumber());

        // 判断是否超期
        boolean isOverdue = isGoodOverdue(good.getBorrowStartTime(), good.getBorrowDuration());
        holder.textViewOverdue.setText(isOverdue ? "超期" : "未超期");
    }

    @Override
    public int getItemCount() {
        return goodsList == null ? 0 : goodsList.size();
    }

    public void updateGoods(List<Good> newGoodsList) {
        this.goodsList = newGoodsList;
        notifyDataSetChanged();
    }

    // 判断物品是否超期
    private boolean isGoodOverdue(String borrowStartTime, int borrowDuration) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long currentTime = System.currentTimeMillis();
        try {
            Date borrowStartDate = dateFormat.parse(borrowStartTime);
            long dueTime = borrowStartDate.getTime() + borrowDuration * 24 * 60 * 60 * 1000L;
            if (dueTime < currentTime) {
                return true; // 如果超期，返回 true
            }else{
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false; // 如果日期解析失败，默认不超期
    }

    static class GoodViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName;
        TextView textViewCabinetNumber;
        TextView textViewOverdue;

        GoodViewHolder(View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName); // 使用你的控件 ID
            textViewCabinetNumber = itemView.findViewById(R.id.textViewCabinetNumber);
            textViewOverdue = itemView.findViewById(R.id.textViewOverdue);
        }
    }
}
