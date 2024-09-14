package com.example.keyadministrator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {

    private List<Item> items;
    private final Context context;

    public ItemsAdapter(List<Item> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false); // 替换为你实际的 item 布局文件
        return new ItemViewHolder(itemView);}

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        ImageView imageView = holder.userImageView.findViewById(R.id.userImageView);
        Glide.with(context)
                .load(item.getAvatarResId()) // 加载本地图片路径
                .into(imageView);
        holder.userNameTextView.setText(item.getUserName());
        holder.statueTextView.setText(item.getStatue());
        holder.operationTypeTextView.setText(item.getOperationType());
        holder.cabinetNumberTextView.setText(item.getCabinetNumber());
        holder.timeTextView.setText(item.getTime());
        // ... 绑定其他数据
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView userImageView;
        TextView userNameTextView;
        TextView statueTextView;
        TextView operationTypeTextView;
        TextView cabinetNumberTextView;
        TextView timeTextView;
        // ... 其他控件

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            // 初始化控件
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            statueTextView = itemView.findViewById(R.id.toolsStatueTextView);
            operationTypeTextView = itemView.findViewById(R.id.operationTypeTextView);
            cabinetNumberTextView = itemView.findViewById(R.id.cabinetNumberTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            userImageView = itemView.findViewById(R.id.userImageView);
            // ... 初始化其他控件
        }
    }
}