package com.example.keyadministrator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TableBorrowAdapter extends RecyclerView.Adapter<TableBorrowAdapter.ViewHolder> {

    public interface OnBorrowButtonClickListener {
        void onBorrowButtonClick(String cabinetName); // 添加位置参数以标识点击的按钮
    }

    private List<TableData> items;
    private final Context context;
    private OnBorrowButtonClickListener listener;

    public TableBorrowAdapter(List<TableData> items, Context context) {
        this.items = items;
        this.context = context;
    }

    public void setOnBorrowButtonClickListener(OnBorrowButtonClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<TableData> newItems){
        this.items = newItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_borrow_table, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableData item = items.get(position);
        ImageView imageView = holder.itemView.findViewById(R.id.item_image);
                Glide.with(context)
                .load(item.imagePath) // 加载本地图片路径
                .into(imageView);
        holder.textViewFirst.setText(item.text);
        holder.textViewSecond.setText(item.storage);
        holder.textViewThird.setText(item.lend);
        holder.textViewFourth.setText(item.total);
        holder.borrowButton.setOnClickListener(v-> {
            if (listener != null) {
                listener.onBorrowButtonClick(item.getBelongs()); // 调用接口方法并传递位置
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewFirst;
        public TextView textViewSecond;
        public TextView textViewThird;
        public TextView textViewFourth;
        public Button borrowButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textViewFirst = itemView.findViewById(R.id.item_text1);
            textViewSecond = itemView.findViewById(R.id.item_text2);
            textViewThird = itemView.findViewById(R.id.item_text3);
            textViewFourth = itemView.findViewById(R.id.item_text4);
            borrowButton = itemView.findViewById(R.id.item_borrow_button);
        }
    }
}
