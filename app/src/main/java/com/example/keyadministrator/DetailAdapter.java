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

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    private final List<TableData> detailData;

    public DetailAdapter(List<TableData> detailData) {
        this.detailData = detailData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TableData item = detailData.get(position);
        ImageView imageView = holder.itemView.findViewById(R.id.item_image);
        Glide.with(holder.itemView.getContext())
                .load(item.imagePath).into(imageView);
        holder.textViewFirst.setText(item.text);
        holder.textViewSecond.setText(item.storage);
        holder.textViewThird.setText(item.lend);
        holder.textViewFourth.setText(item.total);
    }

    @Override
    public int getItemCount() {
        return detailData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewFirst;
        public TextView textViewSecond;
        public TextView textViewThird;
        public TextView textViewFourth;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textViewFirst = itemView.findViewById(R.id.item_text1);
            textViewSecond = itemView.findViewById(R.id.item_text2);
            textViewThird = itemView.findViewById(R.id.item_text3);
            textViewFourth = itemView.findViewById(R.id.item_text4);
        }
    }
}