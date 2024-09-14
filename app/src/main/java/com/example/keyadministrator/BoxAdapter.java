package com.example.keyadministrator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BoxAdapter extends RecyclerView.Adapter<BoxAdapter.ViewHolder> {

    private final List<BoxData> items;

    public BoxAdapter(List<BoxData> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_box, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BoxData item = items.get(position);
        holder.imageView.setImageResource(item.imageResId);
        holder.textView1.setText(item.text1);
        holder.textView2.setText(item.text2);
        holder.textView3.setText(item.text3);
        holder.textView4.setText(item.text4);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView1;
        public TextView textView2;
        public TextView textView3;
        public TextView textView4;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            textView1 = itemView.findViewById(R.id.item_text1);
            textView2 = itemView.findViewById(R.id.item_text2);
            textView3 = itemView.findViewById(R.id.item_text3);
            textView4 = itemView.findViewById(R.id.item_text4);
        }
    }
}