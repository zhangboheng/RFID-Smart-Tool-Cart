package com.example.keyadministrator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FullBoxAdapter extends RecyclerView.Adapter<FullBoxAdapter.ViewHolder> {

    private List<FullBoxData> items;
    private final OnItemClickListener onItemClickListener;

    public FullBoxAdapter(List<FullBoxData> items, OnItemClickListener onItemClickListener) {
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    public void setItems(List<FullBoxData> newItems) {
        this.items =newItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_box_full, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FullBoxData item = items.get(position);
        ImageView imageView = holder.itemView.findViewById(R.id.item_image);
        Glide.with(holder.itemView.getContext())
                .load(item.imagePath) // 加载本地图片路径
                .into(imageView);
        holder.textView0.setText(item.text0);
        holder.textView1.setText(item.text1);
        holder.textView2.setText(item.text2);
        holder.textView3.setText(item.text3);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView0;
        public TextView textView1;
        public TextView textView2;
        public TextView textView3;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textView0 = itemView.findViewById(R.id.item_text0);
            textView1 = itemView.findViewById(R.id.item_text1);
            textView2 = itemView.findViewById(R.id.item_text2);
            textView3 = itemView.findViewById(R.id.item_text3);
        }
    }

    // 点击监听器接口
    public interface OnItemClickListener {
        void onItemClick(FullBoxData item);
    }
}