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

public class GoodStoreAdapter  extends RecyclerView.Adapter<GoodStoreAdapter.GoodViewHolder>{
    private List<Good> goodsList;

    public GoodStoreAdapter(List<Good> goodsList) {
        this.goodsList = goodsList;
    }

    @NonNull
    @Override
    public GoodStoreAdapter.GoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_good_store, parent, false);
        return new GoodStoreAdapter.GoodViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GoodStoreAdapter.GoodViewHolder holder, int position) {
        Good good = goodsList.get(position);
        holder.textViewItemName.setText(String.valueOf(good.getId()));
        holder.textViewCabinetNumber.setText(good.getTypeName());
        holder.textViewOverdue.setText(good.getBelongingCabinetNumber());
    }

    @Override
    public int getItemCount() {
        return goodsList == null ? 0 : goodsList.size();
    }

    public void updateGoods(List<Good> newGoodsList) {
        this.goodsList = newGoodsList;
        notifyDataSetChanged();
    }

    static class GoodViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName;
        TextView textViewCabinetNumber;
        TextView textViewOverdue;

        GoodViewHolder(View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewCabinetNumber = itemView.findViewById(R.id.textViewCabinetNumber);
            textViewOverdue = itemView.findViewById(R.id.textViewOverdue);
        }
    }
}
