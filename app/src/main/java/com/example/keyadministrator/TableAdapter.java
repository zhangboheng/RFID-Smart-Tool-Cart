package com.example.keyadministrator;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TableAdapter extends RecyclerView.Adapter<TableAdapter.ViewHolder> {

    private final List<TableData> items;
    private final Context context;

    public TableAdapter(List<TableData> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_table, parent, false);
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
        holder.detailButton.setOnClickListener(v-> {
            Activity activity = (Activity) holder.itemView.getContext();
            RecyclerView tableRecyclerView2 = activity.findViewById(R.id.tableRecyclerView2);
            RecyclerView detailRecyclerView = activity.findViewById(R.id.detailRecyclerView);

            // 获取圆形按钮
            FloatingActionButton roundButton = activity.findViewById(R.id.roundButton);

            // 显示返回按钮
            roundButton.setVisibility(View.VISIBLE);

            tableRecyclerView2.setVisibility(View.GONE);
            detailRecyclerView.setVisibility(View.VISIBLE);

            // 调用 MainActivity 的方法获取第三个 Table 的数据
            MainActivity mainActivity = (MainActivity) activity;
            List<TableData> thirdTableData = mainActivity.getThirdTableDataForTypeName(items.get(position).text);

            // 设置第三个 RecyclerView 的 Adapter
            detailRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
            DetailAdapter thirdTableAdapter = new DetailAdapter(thirdTableData);
            detailRecyclerView.setAdapter(thirdTableAdapter);
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
        public Button detailButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textViewFirst = itemView.findViewById(R.id.item_text1);
            textViewSecond = itemView.findViewById(R.id.item_text2);
            textViewThird = itemView.findViewById(R.id.item_text3);
            textViewFourth = itemView.findViewById(R.id.item_text4);
            detailButton = itemView.findViewById(R.id.item_detail_button);
        }
    }
}
